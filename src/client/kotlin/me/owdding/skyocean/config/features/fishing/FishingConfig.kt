package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object FishingConfig : CategoryKt("fishing") {
    override val name = Translated("skyocean.config.fishing")

    var enableTrophyNumbers by boolean(false) {
        this.translation = "skyocean.config.fishing.trophy_numbers"
    }

    init {
        obj("hotspot", HotspotHighlightConfig) {

        }
    }

}
