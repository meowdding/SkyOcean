package codes.cookies.skyocean.repo

import codes.cookies.skyocean.utils.Utils
import codes.cookies.skyocean.utils.Utils.exclusiveInclusive
import com.google.gson.JsonArray
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.notkamui.keval.keval
import me.owdding.ktmodules.Module
import net.minecraft.ChatFormatting
import tech.thatgravyboat.skyblockapi.generated.KCodec
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.text.Text

@Module
object HotmData {
    val perks = mutableListOf<HotmPerk>()

    private val PERK_CODEC: Codec<HotmPerk> = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.fieldOf("name").forGetter(HotmPerk::name),
            Codec.INT.fieldOf("max_level").forGetter(HotmPerk::maxLevel),
            Codec.STRING.fieldOf("cost_formula").forGetter(HotmPerk::costFormula),
            KCodec.getCodec<PowderType>().fieldOf("powder_type").forGetter(HotmPerk::powderType),
        ).apply(it, ::HotmPerk)
    }

    init {
        perks.addAll(Utils.loadFromRepo<JsonArray>("hotm").toDataOrThrow(PERK_CODEC.listOf()))
    }

    data class HotmPerk(
        val name: String,
        val maxLevel: Int,
        val costFormula: String,
        val powderType: PowderType,
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

    fun perkByName(name: String): HotmPerk? = perks.firstOrNull { it.name == name }

    enum class PowderType(val formatting: ChatFormatting) {
        MITHRIL(ChatFormatting.DARK_GREEN),
        GEMSTONE(ChatFormatting.LIGHT_PURPLE),
        GLACITE(ChatFormatting.AQUA);

        val displayName by lazy {
            Text.of(name.toTitleCase()) {
                append(" Powder")
                this.withStyle(formatting)
            }
        }
    }
}
