package me.owdding.skyocean.config.features.gambling

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.duration
import me.owdding.skyocean.config.observable
import me.owdding.skyocean.config.separator
import me.owdding.skyocean.features.gambling.vanguard.VanguardGambling
import kotlin.time.DurationUnit

object GamblingConfig : CategoryKt("gambling") {
    override val name = Translated("skyocean.config.gambling")

    init {
        separator("skyocean.config.gambling.dungeons.separator")
    }

    var dungeonsGambling by boolean(false) {
        this.translation = "skyocean.config.gambling.dungeons"
    }

    var gamblingInCroesus by boolean(true) {
        this.translation = "skyocean.config.gambling.dungeons.croesus"
    }

    var dungeonTime by long(5) {
        this.translation = "skyocean.config.gambling.dungeons.time"
        this.slider = true
        this.range = 1L..10
    }.duration(DurationUnit.SECONDS)

    init {
        separator("skyocean.config.gambling.vanguard.separator")
    }

    var vanguardGambling by boolean(false) {
        this.translation = "skyocean.config.gambling.vanguard"
    }

    var vanguardHideChat by boolean(true) {
        this.translation = "skyocean.config.gambling.vanguard.hide_chat"
    }

    var vanguardMode by enum(VanguardGambling.VanguardMode.PREDEFINED) {
        this.translation = "skyocean.config.gambling.vanguard.mode"
    }

    init {
        separator("skyocean.config.gambling.credits")
    }
}
