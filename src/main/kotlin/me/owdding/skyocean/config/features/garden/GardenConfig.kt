package me.owdding.skyocean.config.features.garden

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.duration
import me.owdding.skyocean.helpers.InventorySideGui

object GardenConfig : CategoryKt("garden") {
    override val name = Translated("skyocean.config.garden")

    var pestBaitType by boolean(true) {
        this.translation = "skyocean.config.garden.pest_bait_type"
    }

    var pestBaitAlignment by enum(InventorySideGui.Alignment.RIGHT_OF_INVENTORY) {
        this.translation = "skyocean.config.garden.pest_bait_alignment"
    }

    var deskPestHighlight by boolean(true) {
        this.translation = "skyocean.config.garden.desk_pest_highlight"
    }

    var pestWarning by boolean(true) {
        this.translation = "skyocean.config.garden.pest_warning"
    }

    var pestWarningDelay by long(30) {
        this.translation = "skyocean.config.garden.pest_warning_delay"
        this.slider = true
        this.range = 1L..60L
    }.duration(SECONDS)

}

