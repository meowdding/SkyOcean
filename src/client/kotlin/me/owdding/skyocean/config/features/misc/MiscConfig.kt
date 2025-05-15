package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

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

    init {
        separator {
            title = "skyocean.config.misc.transparentArmor"
            description = "skyocean.config.misc.transparentArmor.desc"
        }
    }

    var transparentArmorSelf by transform(
        int(100) {
            slider = true
            range = 0..100
            translation = "skyocean.config.misc.transparentArmor.self"
        },
        ::from8BitChannel, ::to8BitChannel,
    )

    var transparentArmorOthers by transform(
        int(100) {
            slider = true
            range = 0..100
            translation = "skyocean.config.misc.transparentArmor.others"
        },
        ::from8BitChannel, ::to8BitChannel,
    )

    fun from8BitChannel(int: Int): Int {
        return (int / 255.0).toInt()
    }

    fun to8BitChannel(percentage: Int): Int {
        return ((255 / 100.0) * percentage).toInt()
    }
}

