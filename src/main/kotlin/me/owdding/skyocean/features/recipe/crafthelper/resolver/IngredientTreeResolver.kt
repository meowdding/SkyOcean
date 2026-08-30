package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import me.owdding.skyocean.features.recipe.crafthelper.data.IngredientCraftHelperRecipe

object IngredientTreeResolver : TreeResolver<IngredientCraftHelperRecipe> {
    override val type: CraftHelperRecipeType get() = CraftHelperRecipeType.NORMAL

    override fun resolve(recipe: IngredientCraftHelperRecipe, resetLayout: () -> Unit, clear: () -> Unit): CraftHelperTree? {
        return CraftHelperTree(
            recipe,
            recipe.output,
            CraftHelperStorage.selectedAmount.coerceAtLeast(1),
        )
    }
}
