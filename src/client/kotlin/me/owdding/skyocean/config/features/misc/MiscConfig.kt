package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object MiscConfig : CategoryKt("misc") {
    override val name get() = Translated("skyocean.config.misc")

    var ministerInCalendar by boolean(true) {
        translation = "skyocean.config.misc.ministerInCalendar"
    }
}

