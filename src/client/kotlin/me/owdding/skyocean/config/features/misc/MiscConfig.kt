package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object MiscConfig : CategoryKt("misc") {
    override val name get() = Translated("skyocean.config.misc")

    var ministerInCalendar by boolean(true) {
        translation = "skyocean.config.misc.ministerInCalendar"
    }

    var anvilHelper by boolean(false) {
        translation = "skyocean.config.misc.anvilHelper"
    }

    init {
        separator {
            title = "skyocean.config.misc.transparentArmor"
            description = "skyocean.config.misc.transparentArmor.desc"
        }
    }

    var transparentArmorSelf by int(100) {
        slider = true
        range = 0..100
        translation = "skyocean.config.misc.transparentArmor.self"
    }

    var transparentArmorOthers by int(100) {
        slider = true
        range = 0..100
        translation = "skyocean.config.misc.transparentArmor.others"
    }

    fun transparentArmorSelf8bit() = ((255 / 100.0) * transparentArmorSelf).toInt()
    fun transparentArmorOther8bit() = ((255 / 100.0) * transparentArmorOthers).toInt()
}

