package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object FishingConfig : CategoryKt("fishing") {
    override val name = Translated("skyocean.config.fishing")

    var enableTrophyNumbers by boolean(false) {
        this.translation = "skyocean.config.fishing.trophy_numbers"
    }

    var hookTextScale by float(1f) {
        this.range = 0.25f..5f
        this.slider = true
        this.translation = "skyocean.config.fishing.hook_text_scale"
    }

    init {
        obj("hotspot", HotspotHighlightConfig) {
            this.translation = "skyocean.config.fishing.hotspot"
        }
    }

}
