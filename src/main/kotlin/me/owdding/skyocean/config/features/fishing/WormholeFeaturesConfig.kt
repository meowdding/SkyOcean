package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.utils.transparency

object WormholeFeaturesConfig : ObjectKt() {

    const val LANG_PATH = "skyocean.config.fishing.wormhole"

    var warning by boolean(false) {
        this.translation = "$LANG_PATH.warning"
    }

    init {
        separator {
            this.title = "$LANG_PATH.highlight"
        }
    }

    var circleSurface by boolean(true) {
        this.translation = "$LANG_PATH.circle_surface"
    }

    var circleOutline by boolean(true) {
        this.translation = "$LANG_PATH.circle_outline"
    }

    var circleColumn by boolean(false) {
        this.translation = "$LANG_PATH.circle_column"
    }

    var circleMatchesPlayerY by boolean(false) {
        this.translation = "$LANG_PATH.circle_matches_player_y"
    }


    var hideParticles by boolean(true) {
        this.translation = "$LANG_PATH.hide_particles"
    }

    var color by color(0xAA00AA) {
        this.translation = "$LANG_PATH.color"
    }

    var surfaceTransparency by transparency(50) {
        this.translation = "$LANG_PATH.surface_transparency"
    }

    var outlineTransparency by transparency(100) {
        this.translation = "$LANG_PATH.outline_transparency"
    }

    var columnTransparency by transparency(25) {
        this.translation = "$LANG_PATH.column_transparency"
    }
}
