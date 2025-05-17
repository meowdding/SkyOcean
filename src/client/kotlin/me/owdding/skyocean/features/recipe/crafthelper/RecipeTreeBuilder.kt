package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.skyocean.features.recipe.crafthelper.visitors.RecipeVisitor
import tech.thatgravyboat.repolib.api.recipes.Recipe

data class RecipeTree(val ingredient: ItemLikeIngredient) {

    val recipe: Recipe<*> = SimpleRecipeApi.getBestRecipe(ingredient.skyblockId) ?: throw IllegalStateException("No recipe found for $ingredient")
    val rootNode: RecipeTreeNode = RecipeTreeNode(ingredient)

    fun visit(multiplyChildren: Boolean = false, recipeVisitor: RecipeTreeVisitor) {
        rootNode.visit(recipeVisitor, 0, multiplyChildren)
    }

}

fun interface RecipeTreeVisitor {
    fun visit(node: RecipeTreeNode, depth: Int, children: Int)
    operator fun invoke(node: RecipeTreeNode, depth: Int, children: Int) = visit(node, depth, children)
}

data class RecipeTreeNode(val ingredient: Ingredient) {
    val children: MutableList<RecipeTreeNode> by lazy { mutableListOf<RecipeTreeNode>() }

    init {
        if (ingredient is ItemLikeIngredient) {
            ingredient.getRecipe()?.let { recipe ->
                RecipeVisitor.getInputs(recipe).groupBy { it.serialize() }.forEach { (_, ingredient) ->
                    children.add(RecipeTreeNode(ingredient.first().withAmount(ingredient.sumOf { it.amount })))
                }
            }
        }
    }

    fun visit(recipeVisitor: RecipeTreeVisitor, depth: Int, multiplyChildren: Boolean) {
        recipeVisitor(this, depth, children.size)
        for (i in 0 until this.ingredient.amount) {
            children.forEach { it.visit(recipeVisitor, depth + 1, multiplyChildren) }
            if (multiplyChildren) break
        }
    }
}
