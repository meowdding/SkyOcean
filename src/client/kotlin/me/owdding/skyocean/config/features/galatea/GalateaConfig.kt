package me.owdding.skyocean.config.features.galatea

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory
import me.owdding.skyocean.config.defaultEnabledMessage
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.world.item.DyeColor
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@ConfigCategory
object GalateaConfig : CategoryKt("galatea") {
    override val name get() = Translated("skyocean.config.galatea")

    var muteThePhantoms by defaultEnabledMessage(
        boolean(true) {
            translation = "skyocean.config.galatea.muteThePhantoms"
        },
        { +"skyocean.config.galatea.muteThePhantoms.warning" },
        "mute_the_fucking_phantoms",
        predicate = { SkyBlockIsland.GALATEA.inIsland() },
    )

    var shulkerOverwrite by enum<DyeColor>(DyeColor.GREEN) {
        translation = "skyocean.config.galatea.shulkerOverwrite"
    }

}
