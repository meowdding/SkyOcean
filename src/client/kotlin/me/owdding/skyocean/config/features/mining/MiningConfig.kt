package me.owdding.skyocean.config.features.mining

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object MiningConfig : CategoryKt("mining") {
    init {
        category(MineshaftConfig)
        category(MiningRetexture)
    }

    override val name get() = Translated("skyocean.config.mining")

    var modifyCommissions by boolean(true) {
        translation = "skyocean.config.mining.modifyCommissions"
    }

    var hotmStackSize by boolean(true) {
        translation = "skyocean.config.mining.hotm.stackSize"
    }

    var hotmTotalProgress by boolean(true) {
        translation = "skyocean.config.mining.hotm.totalProgress"
    }

    var hotmDisplayShiftCost by boolean(true) {
        translation = "skyocean.config.mining.hotm.shiftCost"
    }

    var hotmDisplayTotalLeft by boolean(true) {
        translation = "skyocean.config.mining.hotm.totalLeft"
    }

    var chAreaWalls by boolean(false) {
        translation = "skyocean.config.mining.ch.area_walls"
    }
}
