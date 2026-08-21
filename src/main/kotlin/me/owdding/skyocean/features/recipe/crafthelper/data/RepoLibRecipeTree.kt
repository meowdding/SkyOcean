package me.owdding.skyocean.features.recipe.crafthelper.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.features.recipe.RepoApiRecipe
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.resolver.RepoLibTreeResolver

@GenerateCodec
data class RepoLibRecipeTree(
    var recipe: RepoApiRecipe,
) : CraftHelperRecipe(CraftHelperRecipeType.REPO_LIB_RECIPE, true) {
    override fun resolve(resetLayout: () -> Unit, clear: () -> Unit): CraftHelperTree? {
        return RepoLibTreeResolver.resolve(this, resetLayout, clear)
    }
}
