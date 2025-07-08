package me.owdding.skyocean.repo

import com.google.gson.JsonArray
import com.notkamui.keval.keval
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.exclusiveInclusive
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow

@Module
object HotfData {
    val perks = mutableListOf<HotfPerk>()

    init {
        perks.addAll(Utils.loadFromRepo<JsonArray>("hotf").toDataOrThrow(SkyOceanCodecs.HotfPerkCodec.codec().listOf()))
    }

    @GenerateCodec
    data class HotfPerk(
        val name: String,
        @FieldName("max_level") val maxLevel: Int,
        @FieldName("cost_formula") val costFormula: String,
        @FieldName("powder_type") val powderType: PowderType,
    ) {

        fun totalPowder() = powderForInterval(1 exclusiveInclusive maxLevel)
        fun powderForInterval(intRange: IntRange) = intRange.sumOf { calculatePowder(it) }

        fun calculatePowder(level: Int): Int {
            return costFormula.keval {
                includeDefault()
                constant {
                    name = "nextLevel"
                    value = level.toDouble()
                }
            }.toInt()
        }
    }

    fun perkByName(name: String): HotfPerk? = perks.firstOrNull { it.name == name }
}

