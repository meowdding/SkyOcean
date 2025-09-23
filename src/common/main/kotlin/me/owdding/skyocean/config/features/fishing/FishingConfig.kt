package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.requiresChunkRebuild

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

    var lavaReplacement by requiresChunkRebuild(
        boolean(false) {
            this.translation = "skyocean.config.fishing.lava_replacement"
        }
    )

    var fixBobber by boolean(true) {
        this.translation = "skyocean.config.fishing.bobber_fix"
    }

    var bobberTime by boolean(false) {
        this.translation = "skyocean.config.fishing.bobber_time"
    }

    var hideOtherBobbers by boolean(false) {
        this.translation = "skyocean.config.fishing.hide_other_bobbers"
    }

    init {
        obj("hotspot", HotspotFeaturesConfig) {
            this.translation = "skyocean.config.fishing.hotspot"
        }
    }

}
