package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.utils.transparency

object WormholeFeaturesConfig : ObjectKt() {

    override val baseTranslation: String = "skyocean.config.fishing.wormhole"

    var warning by boolean(false) {
        this.translation = "warning"
    }

    init {
        separator {
            this.title = "highlight"
        }
    }

    var circleSurface by boolean(true) {
        this.translation = "circle_surface"
    }

    var circleOutline by boolean(true) {
        this.translation = "circle_outline"
    }

    var circleColumn by boolean(false) {
        this.translation = "circle_column"
    }

    var circleMatchesPlayerY by boolean(false) {
        this.translation = "circle_matches_player_y"
    }


    var hideParticles by boolean(true) {
        this.translation = "hide_particles"
    }

    var color by color(0xAA00AA) {
        this.translation = "color"
    }

    var surfaceTransparency by transparency(50) {
        this.translation = "surface_transparency"
    }

    var outlineTransparency by transparency(100) {
        this.translation = "outline_transparency"
    }

    var columnTransparency by transparency(25) {
        this.translation = "column_transparency"
    }
}
