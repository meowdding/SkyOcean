package me.owdding.skyocean.config.features.inventory

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.helpers.InventorySideGui

object SackValueConfig : ObjectKt(), Translatable {
    private const val PATH = "skyocean.config.inventory.sack_value"
    override fun getTranslationKey(): String = "$PATH.edit"

    var enabled by boolean(true) {
        this.translation = PATH
    }

    var alignment by enum(InventorySideGui.Alignment.RIGHT_OF_INVENTORY) {
        this.translation = "$PATH.alignment"
    }

    var priceSource by enum(PriceSource.BAZAAR) {
        this.translation = "$PATH.price_source"
    }

    var showInSackOfSacks by boolean(true) {
        this.translation = "$PATH.show_in_sack_of_sacks"
    }

    var hideItemsWithNoValue by boolean(true) {
        this.translation = "$PATH.hide_items_with_no_value"
    }

    var showTotalValue by boolean(true) {
        this.translation = "$PATH.show_total_value"
    }

    enum class PriceSource : Translatable {
        BAZAAR,
        NPC,
        ;

        override fun getTranslationKey() = "$PATH.price_source.${this.name.lowercase()}"
    }
}
