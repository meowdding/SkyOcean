package me.owdding.skyocean.features.recipe

import me.owdding.skyocean.features.recipe.visitors.RecipeVisitor
import tech.thatgravyboat.repolib.api.recipes.Recipe as RepoRecipe

interface Recipe {
    val inputs: List<Ingredient>
    val output: ItemLikeIngredient?
    val recipeType: RecipeType
}

interface ParentRecipe : Recipe {
    fun getRecipe(ingredient: Ingredient): Recipe?
}

data class RepoApiRecipe(val recipe: RepoRecipe<*>, override val recipeType: RecipeType) : Recipe {
    override val inputs: List<Ingredient> by lazy { RecipeVisitor.getInputs(recipe) }
    override val output: ItemLikeIngredient? by lazy { RecipeVisitor.getOutput(recipe) }
}
