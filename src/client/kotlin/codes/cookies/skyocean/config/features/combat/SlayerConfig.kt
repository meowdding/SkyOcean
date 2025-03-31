package codes.cookies.skyocean.config.features.combat

import codes.cookies.skyocean.config.translation
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object SlayerConfig: CategoryKt("slayer") {
    override val name = Translated("skyocean.config.slayer")

    var enableBlazeHighlight by boolean(true) {
        this.translation = "skyocean.config.slayer.highlights.blaze"
    }

    var highlightOwnBoss by boolean(true) {
        this.translation = "skyocean.config.slayer.highlight_own_boss"
    }

    var highlightMini by boolean(true) {
        this.translation = "skyocean.config.slayer.highlight.mini"
    }

    var highlightBigBoys by boolean(true) {
        this.translation = "skyocean.config.slayer.highlight.mini.big"
    }
}
