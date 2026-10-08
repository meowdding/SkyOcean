package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import me.owdding.skyocean.features.recipe.crafthelper.data.RepoLibRecipeTree

object RepoLibTreeResolver : TreeResolver<RepoLibRecipeTree> {
    override val type: CraftHelperRecipeType get() = REPO_LIB_RECIPE

    override fun resolve(recipe: RepoLibRecipeTree, resetLayout: () -> Unit, clear: () -> Unit): CraftHelperTree? {
        return CraftHelperTree(recipe.recipe, recipe.recipe.output ?: return null, CraftHelperStorage.selectedAmount.coerceAtLeast(1))
    }
}
