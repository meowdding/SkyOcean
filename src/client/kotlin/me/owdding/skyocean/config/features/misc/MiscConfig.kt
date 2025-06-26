package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory
import me.owdding.skyocean.config.defaultEnabledMessage
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.transparency
import net.minecraft.world.item.DyeColor
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@ConfigCategory
object MiscConfig : CategoryKt("misc") {
    override val name get() = Translated("skyocean.config.misc")

    var muteThePhantoms by defaultEnabledMessage(
        boolean(true) {
            translation = "skyocean.config.misc.muteThePhantoms"
        },
        { +"skyocean.config.misc.muteThePhantoms.warning" },
        "mute_the_fucking_phantoms",
        predicate = { SkyBlockIsland.GALATEA.inIsland() },
    )

    var shulkerOverwrite by enum<DyeColor>(DyeColor.GREEN) {
        translation = "skyocean.config.misc.shulkerOverwrite"
    }

    var ministerInCalendar by boolean(true) {
        translation = "skyocean.config.misc.ministerInCalendar"
    }

    var previousServer by boolean(false) {
        translation = "skyocean.config.misc.previousServer"
    }

    var previousServerTime by int(360) {
        translation = "skyocean.config.misc.previousServerTime"
    }

    var anvilHelper by boolean(false) {
        translation = "skyocean.config.misc.anvilHelper"
    }

    var hideLightning by boolean(false) {
        translation = "skyocean.config.misc.hideLightning"
    }

    var showHiddenPetCandy by boolean(true) {
        translation = "skyocean.config.misc.showHiddenPetCandy"
    }

    init {
        separator {
            title = "skyocean.config.misc.transparentArmor"
            description = "skyocean.config.misc.transparentArmor.desc"
        }
    }

    var transparentArmorSelf by transparency(100) {
        translation = "skyocean.config.misc.transparentArmor.self"
    }

    var transparentArmorOthers by transparency(100) {
        translation = "skyocean.config.misc.transparentArmor.others"
    }
}

