package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory
import me.owdding.skyocean.features.misc.buttons.ButtonConfigScreen
import me.owdding.skyocean.utils.transparency
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen

@ConfigCategory
object MiscConfig : CategoryKt("misc") {
    override val name get() = Translated("skyocean.config.misc")

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

    init {
        separator {
            title = "skyocean.config.misc.inventoryButtons"
            description = "skyocean.config.misc.inventoryButtons.desc"
        }
    }

    var inventoryButtons by boolean(true) {
        translation = "skyocean.config.misc.inventoryButtons.enabled"
    }

    init {
        button {
            title = "skyocean.config.misc.inventoryButtons.edit"
            text = "Open"
            description = "skyocean.config.misc.inventoryButtons.edit.desc"
            onClick {
                McClient.setScreen(McScreen.self?.let { ButtonConfigScreen(it) })
            }
        }
    }
}

