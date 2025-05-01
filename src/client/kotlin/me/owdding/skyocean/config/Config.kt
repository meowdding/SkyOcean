package me.owdding.skyocean.config

import com.google.gson.JsonObject
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.config.features.combat.SlayerConfig
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.config.patcher.ConfigPatches
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

    override val patches: Map<Int, UnaryOperator<JsonObject>> = ConfigPatches.loadPatches()
    override val version: Int = patches.size

}
