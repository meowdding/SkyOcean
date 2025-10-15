package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.crafthelper.*
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsRecipeElement
import me.owdding.skyocean.utils.extensions.toIngredient

object SkyShardsTreeResolver : TreeResolver<SkyShardsRecipe> {
    override val type: CraftHelperRecipeType get() = CraftHelperRecipeType.SKY_SHARDS

    override fun resolve(recipe: SkyShardsRecipe, resetLayout: () -> Unit, clear: () -> Unit): Pair<ContextAwareRecipeTree, ItemLikeIngredient> {
        val tree = recipe.tree.visitElements<StandardRecipeNode>(null) { parent, self ->
            val parent = parent as? NodeWithChildren
            if (parent == null) {
                ContextAwareRecipeTree(null, self.shard.toIngredient(self.quantity))
            } else if (self is SkyShardsRecipeElement) {
                RecipeNode(null, self.quantity, self.quantity, self.quantity, 0, self.shard.toIngredient(self.quantity), RecipeRemainder.EMPTY).apply {
                    parent.addChild(this)
                }
            } else {
                LeafNode(self.shard.toIngredient(self.quantity)).apply {
                    parent.addChild(this)
                }
            }
        } as ContextAwareRecipeTree
        return tree to tree.output
    }
}
