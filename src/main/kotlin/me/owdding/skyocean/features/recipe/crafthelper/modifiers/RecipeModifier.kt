package me.owdding.skyocean.features.recipe.crafthelper.modifiers

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.recipe.RecipeType
import me.owdding.skyocean.features.recipe.RepoApiRecipe
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.RepoLibRecipeTree
import me.owdding.skyocean.features.recipe.mergeSameTypes
import me.owdding.skyocean.features.recipe.toCraftingIngredient
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.extensions.toIngredient
import net.minecraft.world.item.Items
import tech.thatgravyboat.repolib.api.recipes.CraftingRecipe
import tech.thatgravyboat.repolib.api.recipes.ingredient.ItemIngredient
import tech.thatgravyboat.skyblockapi.api.datatype.DataType
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.container.ContainerRegion
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.get

@CraftHelperModifier
object RecipeModifier : AbstractCraftHelperModifier() {

    private val region = ContainerRegion(1..3, 1..3)

    override fun applies(event: InventoryChangeEvent): RepoLibRecipeTree? {
        if (event.slot.index != 14) return null
        if (event.inventory.size < 23 || event.inventory[23].item !in Items.CRAFTING_TABLE) return null
        if (event.inventory.size < 32 || event.inventory[32].item.cleanName != "Supercraft") return null
        val outputItem = event.inventory[25].item
        val output = outputItem.getSkyBlockId()?.toIngredient(amount = outputItem.count) ?: return null

        if (event.item !in ItemTag.GLASS_PANES) {
            SkyOcean.warn("Failed to place craft helper item in recipe, item is not a glass pane")
            return null
        }

        val inputIds = event.inventory.mapNotNull {
            if (region.contains(it)) {
                it.item.getSkyBlockId()?.toIngredient(amount = it.item.count)
            } else null
        }.mergeSameTypes()

        return RepoLibRecipeTree(
            RepoApiRecipe(
                CraftingRecipe(inputIds.mapNotNull { it.toCraftingIngredient() }, output.toCraftingIngredient() ?: return null),
            ),
            output.amount,
        )
    }
}
