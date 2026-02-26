package me.owdding.skyocean.repo.museum

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.replaceTrim
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

@LateInitModule
object MuseumRepoData : MeowddingLogger by SkyOcean.featureLogger() {

    private val prefixRegex = Regex("[✖✔]")

    val armor: List<MuseumArmour>
    val weapons: List<MuseumItem>
    val rarities: List<MuseumItem>
    val allItems: List<MuseumItem> by lazy { listOf(weapons, rarities).flatten() }
    val armorNameExceptions: Map<String, String>
    val itemNameExceptions: Map<String, String>

    @GenerateCodec
    @NamedCodec("MuseumData")
    data class Data(
        val armor: List<MuseumArmour>,
        @NamedCodec("museum§item") val weapons: List<MuseumItem>,
        @NamedCodec("museum§item") val rarities: List<MuseumItem>,
    )

    init {
        var armor: List<MuseumArmour>? = null
        var weapons: List<MuseumItem>? = null
        var rarities: List<MuseumItem>? = null
        var armorNameExceptions: Map<String, String>? = null
        var itemNameExceptions: Map<String, String>? = null

        try {
            Utils.loadRepoData<Data>("museum_data").let {
                armor = it.armor
                weapons = it.weapons
                rarities = it.rarities
            }
            val exceptions = Utils.loadRepoData("museum_exceptions", CodecUtils.map(Codec.STRING, CodecUtils.map(Codec.STRING, Codec.STRING)))
            armorNameExceptions = exceptions["armor"] ?: emptyMap()
            itemNameExceptions = exceptions["items"] ?: emptyMap()
        } catch (exception: Exception) {
            error("Failed to load museum data!", exception)
        }

        this.armor = armor ?: emptyList()
        this.weapons = weapons ?: emptyList()
        this.rarities = rarities ?: emptyList()
        this.armorNameExceptions = armorNameExceptions ?: emptyMap()
        this.itemNameExceptions = itemNameExceptions ?: emptyMap()
    }

    var armorNames: Set<String> = setOf(
        "set",
        "suit",
        "armor",
        "outfit",
        "equipment",
        "'s special armor",
        "'s armor",
        "armor of",
        "tuxedo",
    )

    data class MuseumDataError(val type: Type, override val message: String) : Exception(message) {
        enum class Type {
            ITEM_NOT_FOUND,
            NO_MATCHING_MUSEUM_ITEM,
            ARMOR_NOT_FOUND,
        }
    }

    @Throws(MuseumDataError::class)
    fun getDataByName(name: String): MuseumRepoEntry {
        val name = name.replaceTrim(prefixRegex).lowercase()

        if (armorNames.any { name.contains(it) }) {
            val id = armorNameExceptions.getOrElse(name) {
                armorNames.map { name.replaceTrim(it) }.minBy { it.length }
            }.uppercase().replace(" ", "_")
            val secondaryId = name.uppercase().replace(" ", "_")
            return armor.firstOrNull { it.id == id || it.id == secondaryId } ?: throw MuseumDataError(MuseumDataError.Type.ARMOR_NOT_FOUND, name)
        }

        val id = SkyBlockId.fromName(name, true) ?: throw MuseumDataError(MuseumDataError.Type.ITEM_NOT_FOUND, name)

        return allItems.find { it.skyblockId.cleanId == id.cleanId || it.mappedIds.contains(id.cleanId.uppercase()) }
            ?: throw MuseumDataError(MuseumDataError.Type.NO_MATCHING_MUSEUM_ITEM, "$name - ${id.cleanId}")
    }

}
