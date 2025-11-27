package me.owdding.skyocean.features.recipe.visitors

import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import tech.thatgravyboat.repolib.api.recipes.*

interface RecipeVisitor {

    companion object {
        fun getVisitor(recipe: Recipe<*>): RecipeVisitor? = when (recipe) {
            is ForgeRecipe -> ForgeRecipeVisitor
            is CraftingRecipe -> CraftingRecipeVisitor
            is KatRecipe -> KatRecipeVisitor
            is ShopRecipe -> ShopRecipeVisitor
            else -> null
        }

        fun getInputs(recipe: Recipe<*>) = getVisitor(recipe)?.getInputs(recipe) ?: emptyList()
        fun getOutput(recipe: Recipe<*>) = getVisitor(recipe)?.getOutput(recipe)

    }

    fun getInputs(recipe: Recipe<*>): List<Ingredient>
    fun getOutput(recipe: Recipe<*>): ItemLikeIngredient?

}
