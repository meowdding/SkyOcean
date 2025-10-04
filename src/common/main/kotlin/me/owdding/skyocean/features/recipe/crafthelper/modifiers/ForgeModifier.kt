package me.owdding.skyocean.features.recipe.crafthelper.modifiers

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.utils.Utils.contains
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag

@CraftHelperModifier
object ForgeModifier : AbstractCraftHelperModifier() {

    override fun applies(event: InventoryChangeEvent): SkyOceanItemIngredient? {
        if (event.title != "Confirm Process") return null
        if (event.slot.index != 22 && event.slot.index != 23) return null

        if (event.itemStacks[14] in Items.FURNACE && event.slot.index != 23) return null
        if (event.itemStacks[13] in Items.FURNACE && event.slot.index != 22) return null

        val item = SkyBlockId.fromItem(event.inventory[16].item) ?: return null

        if (event.item !in ItemTag.GLASS_PANES) {
            SkyOcean.warn("Failed to place craft helper item in forge recipe, item is not a glass pane")
            return null
        }

        return SkyOceanItemIngredient(item)
    }

}
