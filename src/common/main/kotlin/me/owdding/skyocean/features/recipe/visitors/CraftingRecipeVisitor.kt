package me.owdding.skyocean.features.recipe.visitors

import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.toSkyOceanIngredient
import tech.thatgravyboat.repolib.api.recipes.CraftingRecipe
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient

object CraftingRecipeVisitor : RecipeVisitor {
    override fun getInputs(recipe: Recipe<*>): List<Ingredient> {
        if (recipe !is CraftingRecipe) return emptyList()

        return recipe.inputs().mapNotNull(CraftingIngredient::toSkyOceanIngredient)
    }

    override fun getOutput(recipe: Recipe<*>): ItemLikeIngredient? {
        if (recipe !is CraftingRecipe) return null

        return recipe.result().toSkyOceanIngredient() as? ItemLikeIngredient
    }
}
