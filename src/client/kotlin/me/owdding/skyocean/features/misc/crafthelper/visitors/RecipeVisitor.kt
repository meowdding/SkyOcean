package me.owdding.skyocean.features.misc.crafthelper.visitors

import me.owdding.skyocean.features.misc.crafthelper.Ingredient
import me.owdding.skyocean.features.misc.crafthelper.ItemLikeIngredient
import tech.thatgravyboat.repolib.api.recipes.CraftingRecipe
import tech.thatgravyboat.repolib.api.recipes.ForgeRecipe
import tech.thatgravyboat.repolib.api.recipes.Recipe

interface RecipeVisitor {

    companion object {
        fun getVisitor(recipe: Recipe<*>): RecipeVisitor? {
            return when (recipe) {
                is ForgeRecipe -> ForgeRecipeVisitor
                is CraftingRecipe -> CraftingRecipeVisitor
                else -> null
            }
        }

        fun getInputs(recipe: Recipe<*>) = getVisitor(recipe)?.getInputs(recipe) ?: emptyList()
        fun getOutput(recipe: Recipe<*>) = getVisitor(recipe)?.getOutput(recipe)

    }

    fun getInputs(recipe: Recipe<*>): List<Ingredient>
    fun getOutput(recipe: Recipe<*>): ItemLikeIngredient?

}
