package me.owdding.skyocean.features.recipe.custom

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.Recipe
import me.owdding.skyocean.features.recipe.RecipeType

@GenerateCodec
data class CustomRecipe(
    override var output: ItemLikeIngredient?,
    override val inputs: MutableList<Ingredient>,
) : Recipe {
    override val recipeType: RecipeType = RecipeType.CUSTOM
}
