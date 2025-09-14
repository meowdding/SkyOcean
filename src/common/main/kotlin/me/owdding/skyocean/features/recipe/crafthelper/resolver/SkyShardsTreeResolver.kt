package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipeType
import me.owdding.skyocean.features.recipe.crafthelper.SkyShardsRecipe

object SkyShardsTreeResolver : TreeResolver<SkyShardsRecipe> {
    override val type: CraftHelperRecipeType get() = CraftHelperRecipeType.SKY_SHARDS

    override fun resolve(recipe: SkyShardsRecipe, resetLayout: () -> Unit, clear: () -> Unit): Pair<ContextAwareRecipeTree, ItemLikeIngredient>? {
        val root = recipe.tree
        val output = SkyOceanItemIngredient(root.shard, root.quantity)

        return ContextAwareRecipeTree(null, output, root.quantity) to output
    }
}
