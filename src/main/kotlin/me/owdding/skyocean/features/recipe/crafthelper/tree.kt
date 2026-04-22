package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.lib.extensions.ceil
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.ParentRecipe
import me.owdding.skyocean.features.recipe.Recipe
import me.owdding.skyocean.features.recipe.SimpleRecipeApi
import me.owdding.skyocean.features.recipe.mergeSameTypes
import me.owdding.skyocean.features.recipe.serialize

sealed interface CraftHelperParentNode : CraftHelperEntry{
    val nodes: MutableList<CraftHelperNode>
    override val recipe: Recipe?

    fun addChild(node: CraftHelperNode) {
        this.nodes.add(node)
        this.nodes.sortBy { it.totalChildren }
        recalculateChildren()
    }

    fun recalculateChildren() {}


    fun evaluateChildren(amount: Int, context: RecipeRemainder, visitedRecipes: Set<Recipe?>) {
        recipe?.inputs?.mergeSameTypes()?.forEach {
            val recipe = (recipe as? ParentRecipe)?.getRecipe(it) ?: SimpleRecipeApi.getBestRecipe(it)
            if (visitedRecipes.contains(recipe)) return@forEach
            val recipeOutput = recipe?.output?.amount ?: 1
            val totalRequired = it.amount * amount
            val carriedOver = context[it].coerceAtMost(totalRequired)
            val requiredAmount = totalRequired - carriedOver
            val carriedOverOver = context[it] - carriedOver
            val craftsRequired = (requiredAmount / recipeOutput.toFloat()).ceil()
            val remainder = (craftsRequired * recipeOutput - requiredAmount).coerceAtLeast(0)
            context[it] = remainder + carriedOverOver

            if (recipe != null) {
                addChild(
                    CraftHelperRecipeNode(recipe, craftsRequired, requiredAmount, totalRequired, carriedOver, it, context).apply {
                        evaluateChildren(visitedRecipes + recipe)
                    },
                )
            } else {
                addChild(CraftHelperLeafNode(it.withAmount(requiredAmount)))
            }
        }
        recalculateChildren()
    }
}

sealed interface CraftHelperEntry {
    val outputWithAmount: Ingredient
    val output: Ingredient
    val amountPerCraft: Int
        get() = 1
    val recipe: Recipe?
        get() = null
    val totalChildren: Int
        get() = 0
}

sealed interface CraftHelperNode: CraftHelperEntry

data class CraftHelperLeafNode(override val output: Ingredient) : CraftHelperNode {
    override val outputWithAmount: Ingredient get() = output
}

data class CraftHelperRecipeNode(
    override val recipe: Recipe?,
    val requiredCrafts: Int,
    val requiredAmount: Int,
    val totalRequired: Int,
    val carriedOver: Int,
    override val output: Ingredient,
    val context: RecipeRemainder,
) : CraftHelperParentNode, CraftHelperNode {
    override val amountPerCraft: Int = recipe?.output?.amount ?: 1
    override val nodes: MutableList<CraftHelperNode> = mutableListOf()
    override var totalChildren: Int = 0
    override val outputWithAmount: Ingredient by lazy { output.withAmount(requiredAmount) }
    override fun recalculateChildren() {
        totalChildren = nodes.size + nodes.sumOf { it.totalChildren }
    }

    fun evaluateChildren(recipes: Set<Recipe?>) = evaluateChildren(requiredCrafts, context, recipes)
}

data class CraftHelperTree(
    override val recipe: Recipe?,
    override val output: ItemLikeIngredient,
    val amount: Int = output.amount,
) : CraftHelperParentNode {
    override val amountPerCraft: Int = recipe?.output?.amount ?: 1
    override val nodes: MutableList<CraftHelperNode> = mutableListOf()

    val context = RecipeRemainder()
    override val outputWithAmount: Ingredient by lazy { output.withAmount(amount) }

    init {
        evaluateChildren(amount / amountPerCraft, context, LinkedHashSet(setOf(recipe)))
    }
}

@JvmInline
value class RecipeRemainder(val map: MutableMap<String, Int> = mutableMapOf()) : MutableMap<String, Int> by map {
    companion object {
        val EMPTY = RecipeRemainder()
    }

    operator fun get(ingredient: Ingredient): Int = this[ingredient.serialize()] ?: 0
    operator fun set(ingredient: Ingredient, value: Int) {
        this[ingredient.serialize()] = value
    }

}
