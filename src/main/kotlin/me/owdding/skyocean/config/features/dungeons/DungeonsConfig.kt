package me.owdding.skyocean.config.features.dungeons

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object DungeonsConfig : CategoryKt("dungeons") {
    override val name = Translated("skyocean.config.dungeons")

    var gamblingScreenEnabled by boolean(false) {
        this.translation = "skyocean.config.dungeons.gambling.enabled"
    }

    var gamblingInCroesus by boolean(true) {
        this.translation = "skyocean.config.dungeons.gambling.croesus"
    }
}
