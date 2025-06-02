package me.owdding.skyocean.config.features.garden

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object GardenConfig : CategoryKt("garden") {
    override val name = Translated("skyocean.config.garden")

    var pestBaitType by boolean(true) {
        this.translation = "skyocean.config.garden.pest_bait_type"
    }

}

