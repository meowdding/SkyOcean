package me.owdding.skyocean.api

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyocean.api.SkyOceanItemId.Companion.DELIMITER
import me.owdding.skyocean.api.SkyOceanItemId.Companion.UNKNOWN
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.api.RepoAttributeAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@JvmInline
@Deprecated("Use SkyBlockId instead", ReplaceWith("tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId"))
value class SkyOceanItemId private constructor(val id: String) {
    companion object {
        private val amountRegex = Regex(".*?x[\\d,]+")
        const val DELIMITER = ":"
        const val ITEM = "item$DELIMITER"

        const val PET = "pet$DELIMITER"
        const val RUNE = "rune$DELIMITER"
        const val ATTRIBUTE = "attribute$DELIMITER"
        const val ENCHANTMENT = "enchantment$DELIMITER"
        const val UNSAFE = "unsafe$DELIMITER"
        const val UNKNOWN = "ocean${DELIMITER}unknown"
        val EMPTY: SkyOceanItemId = item(UNKNOWN)
        private val petRegex = Regex("\\[?lvl \\d+]? (.*)")

        fun item(id: String) = SkyOceanItemId("$ITEM$id".lowercase())
        fun pet(id: String) = SkyOceanItemId("$PET$id".lowercase())
        fun pet(id: String, rarity: String) = SkyOceanItemId("$PET$id$DELIMITER$rarity".lowercase())
        fun rune(id: String) = SkyOceanItemId("$RUNE$id".lowercase())
        fun rune(id: String, level: Int) = SkyOceanItemId("$RUNE$id$DELIMITER$level".lowercase())
        fun attribute(id: String) = SkyOceanItemId("$ATTRIBUTE$id".lowercase())
        fun enchantment(id: String) = SkyOceanItemId("$ENCHANTMENT$id".lowercase())
        fun enchantment(id: String, level: Int) = SkyOceanItemId("$ENCHANTMENT$id$DELIMITER$level".lowercase())

        fun fromItem(item: ItemStack) = item.getSkyOceanItemId()

        fun fromName(name: String, dropLast: Boolean = true): SkyOceanItemId? {
            var name = name.lowercase().stripColor()
            if (name.matches(petRegex)) {
                name = name.replace(petRegex, "$1")
            } else if (name.matches(amountRegex)) {
                name = name.substringBeforeLast(" x")
            }

            return SimpleItemApi.findIdByName(name.trim()) ?: if (dropLast) SimpleItemApi.findIdByName(name.substringBeforeLast(" ").trim()) else null
        }

        fun unknownType(input: String): SkyOceanItemId? {
            val unsafeId = unsafe(input.lowercase())

            fun <T> safe(init: () -> T): T? {
                return runCatching { init() }.getOrNull()
            }

            safe { SimpleItemApi.getItemByIdOrNull(unsafeId) }?.let { return item(input) }
            safe { SimpleItemApi.getPetByIdOrNull(unsafeId) }?.let { return pet(input) }
            safe { SimpleItemApi.getEnchantmentByIdOrNull(unsafeId) }?.let { return enchantment(input) }
            safe { SimpleItemApi.getAttributeByIdOrNull(unsafeId) }?.let { return attribute(input) }
            safe { SimpleItemApi.getRuneByIdOrNull(unsafeId) }?.let { return rune(input) }

            return null
        }

        fun unsafe(id: String) = SkyOceanItemId("$UNSAFE$id".lowercase())

        val CODEC: Codec<SkyOceanItemId> = Codec.STRING.xmap(::SkyOceanItemId, SkyOceanItemId::id)

        @IncludedCodec
        val UNKNOWN_CODEC: Codec<SkyOceanItemId> = Codec.STRING.xmap({ it.lowercase() }, { it })
            .xmap({ unknownType(it) ?: SkyOceanItemId(it) }, { it.id })

        fun ItemStack.getSkyOceanId() = fromItem(this) ?: fromName(this.hoverName.stripped)

    }

    val isItem: Boolean get() = id.startsWith(ITEM)
    val isPet: Boolean get() = id.startsWith(PET)
    val isRune: Boolean get() = id.startsWith(RUNE)
    val isEnchantment: Boolean get() = id.startsWith(ENCHANTMENT)
    val isAttribute: Boolean get() = id.startsWith(ATTRIBUTE)
    val isUnsafe: Boolean get() = id.startsWith(UNSAFE)
    val cleanId: String get() = id.substringAfter(DELIMITER)
    val skyblockId: String
        get() = when {

            isPet -> cleanId.substringBeforeLast(DELIMITER)
            isEnchantment -> {
                "ENCHANTED_BOOK_${cleanId.substringBeforeLast(DELIMITER)}_${cleanId.substringAfterLast(DELIMITER)}"
            }
            isAttribute -> {
                "${RepoAttributeAPI.getAttributeDataById(cleanId)?.shardName()}_SHARD"
            }

            else -> cleanId
        }.uppercase()

    fun trySafe(consumer: (String) -> SkyOceanItemId): SkyOceanItemId = if (isUnsafe) consumer(cleanId) else this

    fun toItem(): ItemStack = when {

        isRune -> getRune()
        isPet -> getPet()
        isItem -> getItem()
        isEnchantment -> getEnchantment()
        isAttribute -> getAttribute()

        else -> ItemStack(Items.BARRIER) {
            set(DataComponents.CUSTOM_NAME, Text.of(id) { this.color = TextColor.RED })
        }
    }

    private fun getItem(): ItemStack = SimpleItemApi.getItemById(this)
    private fun getPet(): ItemStack = SimpleItemApi.getPetById(this)
    private fun getRune(): ItemStack = SimpleItemApi.getRuneById(this)
    private fun getEnchantment(): ItemStack = SimpleItemApi.getEnchantmentById(this)
    private fun getAttribute(): ItemStack = SimpleItemApi.getAttributeById(this)

}

private fun ItemStack.getSkyOceanItemId(): SkyOceanItemId? {
    val data = this.getData(DataTypes.ID)
    return when (data) {
        "RUNE", "UNIQUE_RUNE" -> {
            this.getData(DataTypes.APPLIED_RUNE)?.let { (rune, level) -> "$rune$DELIMITER$level" }.let { it ?: UNKNOWN }
                .let(SkyOceanItemId::rune)
        }

        "PET" -> {
            this.getData(DataTypes.PET_DATA)?.let { (id, _, _, rarity) -> "$id$DELIMITER${rarity.name}" }.let { it ?: UNKNOWN }
                .let(SkyOceanItemId::pet)
        }

        "ENCHANTED_BOOK" -> {
            this.getData(DataTypes.ENCHANTMENTS)?.entries?.firstOrNull()?.let { (key, value) -> "$key$DELIMITER$value" }
                .let { it ?: UNKNOWN }
                .let(SkyOceanItemId::enchantment)
        }

        "ATTRIBUTE_SHARD" -> {
            this.getData(DataTypes.ATTRIBUTES)?.entries?.firstOrNull()?.let { (key, _) -> RepoAttributeAPI.getAttributeDataById(key)?.attributeId }
                .let { it ?: UNKNOWN }.let(SkyOceanItemId::attribute)
        }

        else -> (data)?.let(SkyOceanItemId::item)
    }
}
