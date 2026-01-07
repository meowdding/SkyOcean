package me.owdding.skyocean.features.recipe.visitors

import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.toSkyOceanIngredient
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.repolib.api.recipes.ShopRecipe
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient

object ShopRecipeVisitor : RecipeVisitor {
    override fun getInputs(recipe: Recipe<*>): List<Ingredient> {
        if (recipe !is ShopRecipe) return emptyList()

        return recipe.inputs.mapNotNull(CraftingIngredient::toSkyOceanIngredient)
    }

    override fun getOutput(recipe: Recipe<*>): ItemLikeIngredient? {
        if (recipe !is ShopRecipe) return null

        return recipe.result().toSkyOceanIngredient() as? ItemLikeIngredient
    }
}
