package me.owdding.skyocean.config.features.gambling

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.separator

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

    init {
        separator("skyocean.config.gambling.vanguard.separator")
    }

    var vanguardGambling by boolean(false) {
        this.translation = "skyocean.config.gambling.vanguard"
    }
}
