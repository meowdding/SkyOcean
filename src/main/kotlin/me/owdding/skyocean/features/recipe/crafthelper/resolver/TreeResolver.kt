package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType

interface TreeResolver<RecipeType : CraftHelperRecipe> {
    val type: CraftHelperRecipeType
    fun resolve(recipe: RecipeType, resetLayout: () -> Unit, clear: () -> Unit): Pair<ContextAwareRecipeTree, ItemLikeIngredient>?
}
