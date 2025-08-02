package me.owdding.skyocean.config.features.inventory

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.features.inventory.buttons.ButtonConfigScreen
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen

object InventoryConfig : CategoryKt("inventory") {
    override val name = Translated("skyocean.config.inventory")

    init {
        obj("sackValue", SackValueConfig) { this.translation = "skyocean.config.inventory.sack_value" }
    }

    init {
        separator { title = "skyocean.config.inventory.inventory_buttons" }
    }

    var inventoryButtons by boolean(false) {
        translation = "skyocean.config.inventory.inventory_buttons.enabled"
    }

    init {
        button {
            title = "skyocean.config.inventory.inventory_buttons.edit"
            text = "Open"
            description = "skyocean.config.inventory.inventory_buttons.edit.desc"
            onClick {
                McClient.setScreen(McScreen.self?.let { ButtonConfigScreen(it) })
            }
        }
    }
}
