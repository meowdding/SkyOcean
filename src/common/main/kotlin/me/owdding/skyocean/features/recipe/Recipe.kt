package me.owdding.skyocean.features.recipe

import me.owdding.skyocean.features.recipe.visitors.RecipeVisitor
import tech.thatgravyboat.repolib.api.recipes.Recipe as RepoRecipe

abstract class Recipe {

    abstract val inputs: List<Ingredient>
    abstract val output: ItemLikeIngredient?
    abstract val recipeType: RecipeType

}

data class RepoApiRecipe(val recipe: RepoRecipe<*>, override val recipeType: RecipeType) : Recipe() {
    override val inputs: List<Ingredient> by lazy { RecipeVisitor.getInputs(recipe) }
    override val output: ItemLikeIngredient? by lazy { RecipeVisitor.getOutput(recipe) }
}
