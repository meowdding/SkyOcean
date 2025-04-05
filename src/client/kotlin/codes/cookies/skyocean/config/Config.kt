package codes.cookies.skyocean.config

import codes.cookies.skyocean.SkyOcean
import codes.cookies.skyocean.config.features.chat.ChatConfig
import codes.cookies.skyocean.config.features.combat.SlayerConfig
import codes.cookies.skyocean.config.features.mining.MineshaftConfig
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt

object Config : ConfigKt("skyocean/config") {

    override val name: TranslatableValue = TranslatableValue("SkyOcean")
    override val description: TranslatableValue = TranslatableValue("SkyOcean (v${SkyOcean.VERSION})")
    override val links: Array<ResourcefulConfigLink> = emptyArray()

    init {
        category(MineshaftConfig)
        category(SlayerConfig)
        category(ChatConfig)
    }

}
