package me.owdding.skyocean.config.features.inventory

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory
import me.owdding.skyocean.config.features.garden.SackValueConfig

@ConfigCategory
object InventoryConfig : CategoryKt("inventory") {
    override val name = Translated("skyocean.config.inventory")

    init {
        obj("sack_value", SackValueConfig) { this.translation = "skyocean.config.inventory.sack_value" }
    }
}
