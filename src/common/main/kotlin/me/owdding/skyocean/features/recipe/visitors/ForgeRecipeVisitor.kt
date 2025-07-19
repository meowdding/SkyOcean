package me.owdding.skyocean.features.recipe.visitors

import me.owdding.skyocean.features.recipe.CurrencyIngredient
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.toSkyOceanIngredient
import tech.thatgravyboat.repolib.api.recipes.ForgeRecipe
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient

object ForgeRecipeVisitor : RecipeVisitor {
    override fun getInputs(recipe: Recipe<*>): List<Ingredient> {
        if (recipe !is ForgeRecipe) return emptyList()

        return buildList {
            recipe.inputs().mapNotNull(CraftingIngredient::toSkyOceanIngredient).let(::addAll)
            if (recipe.coins() > 0) add(CurrencyIngredient.coins(recipe.coins()))
        }
    }

    override fun getOutput(recipe: Recipe<*>): ItemLikeIngredient? {
        if (recipe !is ForgeRecipe) return null

        return recipe.result().toSkyOceanIngredient() as? ItemLikeIngredient
    }
}
