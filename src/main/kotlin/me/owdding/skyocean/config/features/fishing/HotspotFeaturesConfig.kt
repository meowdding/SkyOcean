package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.utils.transparency

object HotspotFeaturesConfig : ObjectKt() {

    var warning by boolean(false) {
        this.translation = "skyocean.config.fishing.hotspot.warning"
    }

    init {
        separator {
            this.title = "skyocean.config.fishing.hotspot.highlight"
        }
    }

    var circleSurface by boolean(true) {
        this.translation = "skyocean.config.fishing.hotspot.circle_surface"
    }

    var circleOutline by boolean(true) {
        this.translation = "skyocean.config.fishing.hotspot.circle_outline"
    }

    var surfaceTransparency by transparency(50) {
        this.translation = "skyocean.config.fishing.hotspot.surface_transparency"
    }

    var outlineTransparency by transparency(100) {
        this.translation = "skyocean.config.fishing.hotspot.outline_transparency"
    }
}
