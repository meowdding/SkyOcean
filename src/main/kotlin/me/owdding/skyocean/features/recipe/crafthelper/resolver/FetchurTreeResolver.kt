package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import me.owdding.skyocean.features.recipe.crafthelper.data.FetchurCraftHelperRecipe

object FetchurTreeResolver : TreeResolver<FetchurCraftHelperRecipe> {
    override val type: CraftHelperRecipeType get() = NORMAL

    override fun resolve(recipe: FetchurCraftHelperRecipe, resetLayout: () -> Unit, clear: () -> Unit): CraftHelperTree? {
        return CraftHelperTree(
            recipe,
            recipe.output,
            CraftHelperStorage.selectedAmount.coerceAtLeast(1),
        )
    }
}
