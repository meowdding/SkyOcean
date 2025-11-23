package me.owdding.skyocean.features.recipe.visitors

import me.owdding.skyocean.features.recipe.CurrencyIngredient
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.toSkyOceanIngredient
import tech.thatgravyboat.repolib.api.recipes.KatRecipe
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient

object KatRecipeVisitor : RecipeVisitor {
    override fun getInputs(recipe: Recipe<*>): List<Ingredient> {
        if (recipe !is KatRecipe) return emptyList()


        return buildList {
            recipe.items().mapNotNull(CraftingIngredient::toSkyOceanIngredient).let(::addAll)
            recipe.input().toSkyOceanIngredient()?.let(::add)
            if (recipe.coins() > 0) add(CurrencyIngredient.coins(recipe.coins()))
        }
    }

    override fun getOutput(recipe: Recipe<*>): ItemLikeIngredient? {
        if (recipe !is KatRecipe) return null

        return recipe.output().toSkyOceanIngredient() as? ItemLikeIngredient
    }
}
