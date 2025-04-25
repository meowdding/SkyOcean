package codes.cookies.skyocean.config

import codes.cookies.skyocean.SkyOcean
import codes.cookies.skyocean.config.features.chat.ChatConfig
import codes.cookies.skyocean.config.features.combat.SlayerConfig
import codes.cookies.skyocean.config.features.mining.MiningConfig
import codes.cookies.skyocean.config.features.misc.MiscConfig
import com.google.gson.JsonObject
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import java.util.function.UnaryOperator

object Config : ConfigKt("skyocean/config") {

    override val name: TranslatableValue = TranslatableValue("SkyOcean")
    override val description: TranslatableValue = TranslatableValue("SkyOcean (v${SkyOcean.VERSION})")
    override val links: Array<ResourcefulConfigLink> = emptyArray()

    init {
        category(MiningConfig)
        category(SlayerConfig)
        category(ChatConfig)
        category(MiscConfig)
        separator {
            title = "skyocean.config.main.modifications"
            description = "skyocean.config.main.modifications.desc"
        }
    }

    override val patches: Map<Int, UnaryOperator<JsonObject>> = listOf(
        renameOption("mineshaft", "mining.mineshaft"),
        compoundPatch(
            renameOption("mining.recolorCarpets", "mining.retexture.recolorCarpets"),
            renameOption("mining.customMiningTextures", "mining.retexture.customGlaciteTextures"),
            renameOption("mining.customMist", "mining.retexture.customMist"),
            renameOption("mining.customGemstoneTextures", "mining.retexture.customGemstoneTextures"),
        )
    ).mapIndexed { index, unaryOperator -> index to unaryOperator }.toMap()
    override val version: Int = patches.size

}
