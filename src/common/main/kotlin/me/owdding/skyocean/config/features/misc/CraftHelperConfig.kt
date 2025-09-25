package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.recipe.crafthelper.display.CraftHelperFormat
import me.owdding.skyocean.features.recipe.crafthelper.display.CraftHelperLocation

object CraftHelperConfig : CategoryKt("crafthelper") {
    override val name get() = Translated("skyocean.config.misc.crafthelper")

    var enabled by boolean(true) {
        translation = "skyocean.config.misc.crafthelper.enabled"
    }

    var hideCompleted by boolean(true) {
        translation = "skyocean.config.misc.crafthelper.hideCompleted"
    }

    var parentAmount by boolean(true) {
        translation = "skyocean.config.misc.crafthelper.parentAmount"
    }

    var noRootItems by boolean(false) {
        translation = "skyocean.config.misc.crafthelper.disableRootItems"
    }

    var disallowedSources by select<ItemSources> {
        translation = "skyocean.config.misc.crafthelper.disallowedItemSources"
    }

    var position by enum(CraftHelperLocation.LEFT_OF_INVENTORY) {
        translation = "skyocean.config.misc.crafthelper.position"
    }

    var margin by int(10) {
        translation = "skyocean.config.misc.crafthelper.margin"
    }

    var doneMessage by boolean(false) {
        translation = "skyocean.config.misc.crafthelper.done_message"
    }

    var formatter by enum(CraftHelperFormat.TREE) {
        translation = "skyocean.config.misc.crafthelper.tree_formatter"
    }
}
