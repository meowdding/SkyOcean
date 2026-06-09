package me.owdding.skyocean.config.features.garden

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.duration
import me.owdding.skyocean.config.translation
import me.owdding.skyocean.features.garden.cropfever.CropFeverEffects
import kotlin.time.DurationUnit

object CropFeverEffectsConfig : ObjectKt(), Translatable {

    const val ROOT_PATH = "skyocean.config.garden.crop_fever_effect"
    const val SOUND_PATH = "$ROOT_PATH.sound_section"
    const val VISUAL_PATH = "$ROOT_PATH.visual_section"
    override fun getTranslationKey(): String = "$ROOT_PATH.config_title"

    var enabled by boolean(false) {
        this.translation = "$ROOT_PATH.enabled"
    }

    init {
        separator(SOUND_PATH)
    }

    var startingSound by boolean(true) {
        this.translation = "$SOUND_PATH.startingSound"
    }

    var rngSound by boolean(true) {
        this.translation = "$SOUND_PATH.rngSound"
    }

    var backgroundMusic by boolean(true) {
        this.translation = "$SOUND_PATH.backgroundMusic"
    }

    init {
        separator(VISUAL_PATH)
    }

    var hueShiftingShader by boolean(true) {
        this.translation = "$VISUAL_PATH.hueShiftingShader"
    }

    var shiftingShaderSpeed by enum(CropFeverEffects.ShiftingSpeedOptions.NORMAL) {
        this.translation = "$VISUAL_PATH.hueShiftingShader.shiftingSpeed"
    }

    var coinRain by boolean(true) {
        this.translation = "$VISUAL_PATH.coinRain"
    }

    var coinRainDuration by long(3) {
        this.translation = "$VISUAL_PATH.coinRain.duration"
        this.slider = true
        this.range = 1L..60
    }.duration(DurationUnit.SECONDS)

    var coinRainSpawnMultiplier by enum(CropFeverEffects.CoinRainSpawnMultiplierOptions.AUTO) {
        this.translation = "$VISUAL_PATH.coinRain.spawnMultiplier"
    }

    init {
        separator("$ROOT_PATH.credits")
    }

    private fun EntriesBuilder.separator(translation: String) = separator {
        this.translation = translation
    }
}
