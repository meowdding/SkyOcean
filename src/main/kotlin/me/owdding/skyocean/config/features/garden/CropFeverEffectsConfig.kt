package me.owdding.skyocean.config.features.garden

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.duration
import me.owdding.skyocean.config.translation
import me.owdding.skyocean.features.garden.cropfever.CropFeverEffects
import kotlin.time.DurationUnit

object CropFeverEffectsConfig : ObjectKt(), Translatable  {

    const val PATH = "skyocean.config.garden.crop_fever_effect"
    override fun getTranslationKey(): String = "$PATH.config_title"

    var enabled by boolean(false) {
        this.translation = "$PATH.enabled"
    }

    var backgroundMusic by boolean(true) {
        this.translation = "$PATH.backgroundMusic"
    }

    var coinRain by boolean(true) {
        this.translation = "$PATH.coinRain"
    }

    var coinRainDuration by long(3) {
        this.translation = "$PATH.coinRain.duration"
        this.slider = true
        this.range = 1L..60
    }.duration(DurationUnit.SECONDS)

    var coinRainSpawnMultiplier by enum(CropFeverEffects.CoinRainSpawnMultiplierOptions.AUTO) {
        this.translation = "$PATH.coinRain.spawnMultiplier"
    }

    var startingSound by boolean(true) {
        this.translation = "$PATH.startingSound"
    }

    var hueShiftingShader by boolean(true) {
        this.translation = "$PATH.hueShiftingShader"
    }

    var shiftingShaderSpeed by enum(CropFeverEffects.ShiftingSpeedOptions.NORMAL) {
        this.translation = "$PATH.hueShiftingShader.shiftingSpeed"
    }

    init {
        separator("$PATH.credits")
    }

    private fun EntriesBuilder.separator(translation: String) = separator {
        this.translation = translation
    }
}
