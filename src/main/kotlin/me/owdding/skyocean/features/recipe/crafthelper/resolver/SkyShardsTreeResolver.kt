package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperEntry
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperLeafNode
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperParentNode
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipeNode
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.RecipeRemainder
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsRecipeElement
import me.owdding.skyocean.utils.extensions.toIngredient

object SkyShardsTreeResolver : TreeResolver<SkyShardsRecipe> {
    override val type: CraftHelperRecipeType get() = CraftHelperRecipeType.SKY_SHARDS

    override fun resolve(recipe: SkyShardsRecipe, resetLayout: () -> Unit, clear: () -> Unit): CraftHelperTree {
        val tree = recipe.tree.visitElements<CraftHelperEntry>(null) { parent, self ->
            val parent = parent as? CraftHelperParentNode
            if (parent == null) {
                CraftHelperTree(null, self.shard.toIngredient(self.quantity))
            } else if (self is SkyShardsRecipeElement) {
                CraftHelperRecipeNode(null, self.quantity, self.quantity, self.quantity, 0, self.shard.toIngredient(self.quantity), RecipeRemainder.EMPTY).apply {
                    parent.addChild(this)
                }
            } else {
                CraftHelperLeafNode(self.shard.toIngredient(self.quantity)).apply {
                    parent.addChild(this)
                }
            }
        } as CraftHelperTree
        return tree
    }
}
