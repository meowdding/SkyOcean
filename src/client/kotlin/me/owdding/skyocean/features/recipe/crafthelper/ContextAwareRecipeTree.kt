package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.lib.extensions.ceil
import me.owdding.skyocean.features.recipe.*
import me.owdding.skyocean.features.recipe.visitors.RecipeVisitor
import tech.thatgravyboat.repolib.api.recipes.Recipe

interface ChildlessNode : StandardRecipeNode {
    override val recipe: Recipe<*>?
        get() = null
}

interface NodeWithChildren : StandardRecipeNode {
    override val amountPerCraft: Int
    val nodes: MutableList<StandardRecipeNode>
    override val recipe: Recipe<*>

    fun addChild(node: StandardRecipeNode) {
        this.nodes.add(node)
    }

    fun visit(visitor: (node: StandardRecipeNode, depth: Int) -> Boolean) = visit(0, false, visitor)
    override fun visit(depth: Int, onlyLeafs: Boolean, visitor: (node: StandardRecipeNode, depth: Int) -> Boolean) {
        if (onlyLeafs || visitor(this, depth)) nodes.forEach {
            it.visit(depth + 1, onlyLeafs, visitor)
        }
    }
}

interface StandardRecipeNode {
    val outputWithAmount: Ingredient
    val output: Ingredient
    val amountPerCraft: Int
        get() = 1
    val recipe: Recipe<*>?
        get() = null

    fun evaluateChildren(amount: Int, context: RecipeRemainder) {
        if (this !is NodeWithChildren) return

        RecipeVisitor.getInputs(recipe).mergeSameTypes().forEach {
            val recipe = SimpleRecipeApi.getBestRecipe(it)
            val recipeOutput = recipe?.let { recipe -> RecipeVisitor.getOutput(recipe) }?.amount ?: 1
            val totalRequired = it.amount * amount
            val carriedOver = context[it].coerceAtMost(totalRequired)
            val requiredAmount = totalRequired - carriedOver
            val carriedOverOver = context[it] - carriedOver
            val craftsRequired = (requiredAmount / recipeOutput.toFloat()).ceil()
            val remainder = (craftsRequired * recipeOutput - requiredAmount).coerceAtLeast(0)
            context[it] = remainder + carriedOverOver

            if (recipe != null) {
                addChild(RecipeNode(recipe, craftsRequired, requiredAmount, totalRequired, carriedOver, it, context))
            } else {
                addChild(LeafNode(it.withAmount(requiredAmount)))
            }
        }
    }

    fun visit(depth: Int = 0, onlyLeafs: Boolean = false, visitor: (node: StandardRecipeNode, depth: Int) -> Boolean) {
        visitor(this, depth)
    }
}

data class LeafNode(override val output: Ingredient) : ChildlessNode {
    override val outputWithAmount: Ingredient
        get() = output
}

data class RecipeNode(
    override val recipe: Recipe<*>,
    val requiredCrafts: Int,
    val requiredAmount: Int,
    val totalRequired: Int,
    val carriedOver: Int,
    override val output: Ingredient,
    val context: RecipeRemainder,
) : NodeWithChildren {
    override val amountPerCraft: Int = RecipeVisitor.getOutput(recipe)?.amount ?: 1
    override val nodes: MutableList<StandardRecipeNode> = mutableListOf()
    override val outputWithAmount: Ingredient by lazy { output.withAmount(requiredAmount) }

    init {
        evaluateChildren(requiredCrafts, context)
    }

}

class ContextAwareRecipeTree(override val recipe: Recipe<*>, override val output: ItemLikeIngredient, val amount: Int) :
    NodeWithChildren {
    override val amountPerCraft: Int = RecipeVisitor.getOutput(recipe)?.amount ?: 1
    override val nodes: MutableList<StandardRecipeNode> = mutableListOf()

    val context = RecipeRemainder()
    override val outputWithAmount: Ingredient by lazy { output.withAmount(amount) }

    init {
        evaluateChildren(amount, context)
    }

}

@JvmInline
value class RecipeRemainder(val map: MutableMap<String, Int> = mutableMapOf()) : MutableMap<String, Int> by map {

    operator fun get(ingredient: Ingredient): Int = this[ingredient.serialize()] ?: 0
    operator fun set(ingredient: Ingredient, value: Int) {
        this[ingredient.serialize()] = value
    }

}
