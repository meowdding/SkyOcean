package me.owdding.skyocean.config.features.inventory

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory
import me.owdding.skyocean.features.inventory.buttons.ButtonConfigScreen
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen

@ConfigCategory
object InventoryConfig : CategoryKt("inventory") {
    override val name = Translated("skyocean.config.inventory")

    init {
        obj("sackValue", SackValueConfig) { this.translation = "skyocean.config.inventory.sack_value" }
    }

    init {
        separator { "skyocean.config.inventory.inventory_buttons" }
    }

    var inventoryButtons by boolean(false) {
        "skyocean.config.inventory.inventory_buttons.enabled"
    }

    init {
        button {
            "skyocean.config.inventory.inventory_buttons.edit"
            text = "Open"
            onClick {
                McClient.setScreen(McScreen.self?.let { ButtonConfigScreen(it) })
            }
        }
    }
}
