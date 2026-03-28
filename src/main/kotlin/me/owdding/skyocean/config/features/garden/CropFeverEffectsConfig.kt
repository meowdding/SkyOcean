package me.owdding.skyocean.config.features.garden

import me.owdding.skyocean.config.duration
import me.owdding.skyocean.config.overlays.OverlayConfig
import me.owdding.skyocean.config.separator
import kotlin.time.DurationUnit

object CropFeverEffectsConfig : OverlayConfig("skyocean.config.garden.crop_fever_effect.overlay_title") {

    var enabled by boolean(true) {
        this.translation = "skyocean.config.garden.crop_fever_effect.enabled"
    }

    var backgroundMusic by boolean(true) {
        this.translation = "skyocean.config.garden.crop_fever_effect.backgroundMusic"
    }

    var coinsDropping by boolean(true) {
        this.translation = "skyocean.config.garden.crop_fever_effect.coinsDropping"
    }

    var coinsDroppingDuration by long(5) {
        this.translation = "skyocean.config.garden.crop_fever_effect.coinsDroppingDuration"
        this.slider = true
        this.range = 1L..60
    }.duration(DurationUnit.SECONDS)

    var startingSound by boolean(true) {
        this.translation = "skyocean.config.garden.crop_fever_effect.startingSound"
    }

    var hueShiftingShader by boolean(false) {
        this.translation = "skyocean.config.garden.crop_fever_effect.hueShiftingShader"
    }

    init {
        separator("skyocean.config.garden.crop_fever_effect.credits")
    }
}
