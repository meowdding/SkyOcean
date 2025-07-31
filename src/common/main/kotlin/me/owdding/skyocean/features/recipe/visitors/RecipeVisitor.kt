package me.owdding.skyocean.features.recipe.visitors

import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import tech.thatgravyboat.repolib.api.recipes.CraftingRecipe
import tech.thatgravyboat.repolib.api.recipes.ForgeRecipe
import tech.thatgravyboat.repolib.api.recipes.KatRecipe
import tech.thatgravyboat.repolib.api.recipes.Recipe

interface RecipeVisitor {

    companion object {
        fun getVisitor(recipe: Recipe<*>): RecipeVisitor? {
            return when (recipe) {
                is ForgeRecipe -> ForgeRecipeVisitor
                is CraftingRecipe -> CraftingRecipeVisitor
                is KatRecipe -> KatRecipeVisitor
                else -> null
            }
        }

        fun getInputs(recipe: Recipe<*>) = getVisitor(recipe)?.getInputs(recipe) ?: emptyList()
        fun getOutput(recipe: Recipe<*>) = getVisitor(recipe)?.getOutput(recipe)

    }

    fun getInputs(recipe: Recipe<*>): List<Ingredient>
    fun getOutput(recipe: Recipe<*>): ItemLikeIngredient?

}
