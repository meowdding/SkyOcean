package me.owdding.skyocean.features.misc.crafthelper

import me.owdding.skyocean.features.misc.crafthelper.visitors.RecipeVisitor
import tech.thatgravyboat.repolib.api.recipes.Recipe

data class RecipeTree<T : Recipe<T>>(val recipe: Recipe<T>) {


}

data class RecipeTreeNode(val ingredient: Ingredient) {
    val children: MutableList<RecipeTreeNode> by lazy { mutableListOf<RecipeTreeNode>() }

    init {
        if (ingredient is ItemLikeIngredient) {
            ingredient.getRecipe()?.let { recipe ->
                children.addAll(RecipeVisitor.getInputs(recipe).map { input -> RecipeTreeNode(input) })
            }
        }
    }
}
