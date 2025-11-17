package me.owdding.skyocean.features.recipe.crafthelper.modifiers

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.utils.Utils.contains
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName

@CraftHelperModifier
object RecipeModifier : AbstractCraftHelperModifier() {

    override fun applies(event: InventoryChangeEvent): SkyOceanItemIngredient? {
        if (event.slot.index != 14) return null
        if (event.inventory.size < 23 || event.inventory[23].item !in Items.CRAFTING_TABLE) return null
        if (event.inventory.size < 32 || event.inventory[32].item.cleanName != "Supercraft") return null
        val item = SkyBlockId.fromItem(event.inventory[25].item) ?: return null

        if (event.item !in ItemTag.GLASS_PANES) {
            SkyOcean.warn("Failed to place craft helper item in recipe, item is not a glass pane")
            return null
        }

        return SkyOceanItemIngredient(item)
    }
}
