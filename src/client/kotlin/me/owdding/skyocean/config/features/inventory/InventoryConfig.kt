package me.owdding.skyocean.config.features.inventory

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object InventoryConfig : CategoryKt("inventory") {
    override val name = Translated("skyocean.config.inventory")

    init {
        obj("sackValue", SackValueConfig) { this.translation = "skyocean.config.inventory.sack_value" }
    }
}
