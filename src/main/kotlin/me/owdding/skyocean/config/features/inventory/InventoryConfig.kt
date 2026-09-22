package me.owdding.skyocean.config.features.inventory

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.separator
import me.owdding.skyocean.features.inventory.accessories.AccessoriesHelper
import me.owdding.skyocean.features.inventory.accessories.AccessoriesHelperScreen
import me.owdding.skyocean.features.inventory.buttons.ButtonConfigScreen
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen

object InventoryConfig : CategoryKt("inventory") {
    override val name = Translated("skyocean.config.inventory")

    init {
        obj("sackValue", SackValueConfig) { this.translation = "skyocean.config.inventory.sack_value" }
    }

    var minionHelper by boolean(true) {
        translation = "skyocean.config.inventory.minion_helper"
    }

    init {
        separator("skyocean.config.inventory.accessories_helper.info")
    }

    var accessoriesHelper by boolean(true) {
        translation = "skyocean.config.inventory.accessories_helper"
    }

    var disabledAccessoryIcons by select<AccessoriesHelper.AccessoryResult> {
        translation = "skyocean.config.inventory.accessories_helper.disabled_icons"
    }

    init {
        button {
            title = "skyocean.config.inventory.accessories_helper.open_screen"
            text = "skyocean.config.inventory.accessories_helper.open_screen.text"
            description = "skyocean.config.inventory.accessories_helper.open_screen.desc"

            onClick {
                if (LocationAPI.isOnSkyBlock && !SkyBlockIsland.THE_RIFT.inIsland()) {
                    McClient.setScreen(AccessoriesHelperScreen)
                }
            }
        }
    }

    init {
        separator { title = "skyocean.config.inventory.inventory_buttons" }
    }

    var inventoryButtons by boolean(false) {
        translation = "skyocean.config.inventory.inventory_buttons.enabled"
    }

    var renderBehindBackgroundIfInactive by boolean(true) {
        translation = "skyocean.config.inventory.inventory_buttons.render_behind_background"
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

        separator {
            title = "skyocean.config.inventory.salvaging_helper"
        }
    }

    var salvagingHelper by boolean(false) {
        translation = "skyocean.config.inventory.salvaging_helper.enabled"
        this.searchTerms += listOf("salvage", "salvaging", "helper")
    }

    var salvagingHelperHighlight by boolean(true) {
        translation = "skyocean.config.inventory.salvaging_helper.highlight"
        this.searchTerms += listOf("salvage", "salvaging", "helper")
    }

    var salvagingHelperBlockSalvage by boolean(false) {
        translation = "skyocean.config.inventory.salvaging_helper.block_salvage"
        this.searchTerms += listOf("salvage", "salvaging", "helper")
    }
}
