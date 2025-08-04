package me.owdding.skyocean.config.features.combat

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object SlayerConfig : CategoryKt("slayer") {
    override val name = Translated("skyocean.config.slayer")

    var enableBlazeHighlight by boolean(true) {
        this.translation = "skyocean.config.slayer.highlights.blaze"
    }

    var highlightOwnBoss by boolean(true) {
        this.translation = "skyocean.config.slayer.highlights.own_boss"
    }

    var highlightMini by boolean(true) {
        this.translation = "skyocean.config.slayer.highlights.minis"
    }

    var highlightBigBoys by boolean(true) {
        this.translation = "skyocean.config.slayer.highlights.minis.big_boys"
    }
}
