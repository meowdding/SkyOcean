package me.owdding.skyocean.features.recipe.crafthelper.modifiers

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.IngredientParser
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.extensions.indexOfOrNull
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore

@CraftHelperModifier
object VisitorModifier : AbstractCraftHelperModifier() {

    // TODO
    //  "Shift to add to list" so we can have multiple recipes on one list (for multiple visitors)

    override fun applies(event: InventoryChangeEvent): SkyOceanItemIngredient? {
        if (!SkyBlockIsland.GARDEN.inIsland()) return null
        if (event.slot.index != 22) return null

        val acceptOffer = event.itemStacks[29]
        if (acceptOffer !in Items.GREEN_TERRACOTTA) return null
        if (event.itemStacks[33] !in Items.RED_TERRACOTTA) return null

        if (event.item !in ItemTag.GLASS_PANES) {
            SkyOcean.warn("Failed to place craft helper item in visitor, item is not a glass pane")
            return null
        }

        val lore = acceptOffer.getRawLore()
        val index = lore.indexOfOrNull("Items Required:") ?: return null
        val items = lore.subList(index + 1, lore.indexOfOrNull(String::isBlank) ?: return null)

        if (items.size != 1) return null // Only support single item requests for now

        return IngredientParser.parse(items[0].trim()) as? SkyOceanItemIngredient
    }
}
