package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.utils.transparency

object WormholeFeaturesConfig : ObjectKt() {

    var warning by boolean(false) {
        this.translation = "skyocean.config.fishing.wormhole.warning"
    }

    init {
        separator {
            this.title = "skyocean.config.fishing.wormhole.highlight"
        }
    }

    var circleSurface by boolean(true) {
        this.translation = "skyocean.config.fishing.wormhole.circle_surface"
    }

    var circleOutline by boolean(true) {
        this.translation = "skyocean.config.fishing.wormhole.circle_outline"
    }

    var hideParticles by boolean(true) {
        this.translation = "skyocean.config.fishing.wormhole.hide_particles"
    }

    var color by color(0xAA00AA) {
        this.translation = "skyocean.config.fishing.wormhole.color"
    }

    var surfaceTransparency by transparency(50) {
        this.translation = "skyocean.config.fishing.wormhole.surface_transparency"
    }

    var outlineTransparency by transparency(100) {
        this.translation = "skyocean.config.fishing.wormhole.outline_transparency"
    }
}
