package me.owdding.skyocean.config.features.combat

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder

object SlayerConfig : CategoryKt("slayer") {
    override val name = Translated("skyocean.config.slayer")

    var enableBlazeHighlight by boolean(true) {
        this.translation = "skyocean.config.slayer.highlights.blaze"
        applyRenderChestCondition()
    }

    var highlightOwnBoss by boolean(true) {
        this.translation = "skyocean.config.slayer.highlights.own_boss"
        applyRenderChestCondition()
    }

    var highlightMini by boolean(true) {
        this.translation = "skyocean.config.slayer.highlights.minis"
        applyRenderChestCondition()
    }

    var highlightBigBoys by boolean(true) {
        this.translation = "skyocean.config.slayer.highlights.minis.big_boys"
        applyRenderChestCondition()
    }

    private fun TypeBuilder.applyRenderChestCondition() {
        //? if renderchest {
        //this.condition = { true }
        //?} else {
        this.condition = { false }
        //?}
    }
}
