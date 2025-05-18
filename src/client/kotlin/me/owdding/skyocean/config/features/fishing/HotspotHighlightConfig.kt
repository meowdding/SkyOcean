package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.utils.transparency

object HotspotHighlightConfig : ObjectKt() {

    var circleSurface by boolean(true) {

    }

    var circleOutline by boolean(true) {

    }

    var surfaceTransparency by transparency(50) {

    }

    var outlineTransparency by transparency(100) {

    }
}
