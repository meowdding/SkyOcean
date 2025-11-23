package me.owdding.skyocean.config.features.foraging

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.defaultEnabledMessage
import me.owdding.skyocean.utils.MinecraftColor
import me.owdding.skyocean.utils.Utils.unaryPlus
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

object GalateaConfig : CategoryKt("galatea") {
    override val name get() = Translated("skyocean.config.foraging.galatea")

    var muteThePhantoms by defaultEnabledMessage(
        boolean(true) {
            translation = "skyocean.config.foraging.galatea.muteThePhantoms"
        },
        { +"skyocean.config.foraging.galatea.muteThePhantoms.warning" },
        "mute_the_fucking_phantoms",
        predicate = { SkyBlockIsland.GALATEA.inIsland() },
    )

    var moongladeBeaconColor by boolean(true) {
        translation = "skyocean.config.foraging.galatea.moongladeBeaconColor"
    }

    var shulkerOverwrite by enum(MinecraftColor.GREEN) {
        translation = "skyocean.config.foraging.galatea.shulkerOverwrite"
    }

}
