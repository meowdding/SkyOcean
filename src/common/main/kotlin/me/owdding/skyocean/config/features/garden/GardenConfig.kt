package me.owdding.skyocean.config.features.garden

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object GardenConfig : CategoryKt("garden") {
    override val name = Translated("skyocean.config.garden")

    var pestBaitType by boolean(true) {
        this.translation = "skyocean.config.garden.pest_bait_type"
    }

    var deskPestHighlight by boolean(true) {
        this.translation = "skyocean.config.garden.desk_pest_highlight"
    }

}

