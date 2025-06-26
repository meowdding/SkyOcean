package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.lib.extensions.ceil
import me.owdding.skyocean.features.recipe.crafthelper.visitors.RecipeVisitor
import tech.thatgravyboat.repolib.api.recipes.Recipe

interface ChildlessNode : StandardRecipeNode {
    override val recipe: Recipe<*>?
        get() = null
}

interface NodeWithChildren : StandardRecipeNode {
    val nodes: MutableList<StandardRecipeNode>
    override val recipe: Recipe<*>

    fun addChild(node: StandardRecipeNode) {
        this.nodes.add(node)
    }

    fun visit(visitor: (StandardRecipeNode, Int) -> Unit) = visit(0, visitor)
    override fun visit(depth: Int, visitor: (StandardRecipeNode, Int) -> Unit) {
        super.visit(depth, visitor)
        nodes.forEach {
            it.visit(depth + 1, visitor)
        }
    }
}

interface StandardRecipeNode {
    val output: Ingredient
    val recipe: Recipe<*>?
        get() = null

    fun evaluateChildren(amount: Int, context: RecipeEvaluationContext) {
        if (this !is NodeWithChildren) return

        RecipeVisitor.getInputs(recipe).mergeSameTypes().forEach {
            val recipe = SimpleRecipeApi.getBestRecipe(it)
            val recipeOutput = recipe?.let { recipe -> RecipeVisitor.getOutput(recipe) }?.amount ?: 1
            val requiredAmount = it.amount * amount - context[it]
            val carriedOver = context[it]
            val craftsRequired = (requiredAmount / recipeOutput.toFloat()).ceil()
            val remainder = (craftsRequired * recipeOutput - requiredAmount).coerceAtLeast(0)
            context[it] = remainder

            if (recipe != null) {
                addChild(RecipeNode(recipe, craftsRequired, requiredAmount, carriedOver, it, context))
            } else {
                addChild(LeafNode(it.withAmount(requiredAmount)))
            }
        }
    }

    fun visit(depth: Int = 0, visitor: (node: StandardRecipeNode, depth: Int) -> Unit) {
        visitor(this, depth)
    }
}

data class LeafNode(override val output: Ingredient) : ChildlessNode

data class RecipeNode(
    override val recipe: Recipe<*>,
    val requiredCrafts: Int,
    val requiredAmount: Int,
    val carriedOver: Int,
    override val output: Ingredient,
    val context: RecipeEvaluationContext,
) : NodeWithChildren {
    override val nodes: MutableList<StandardRecipeNode> = mutableListOf()

    init {
        evaluateChildren(requiredCrafts, context)
    }

}

class ContextAwareRecipeTree(override val recipe: Recipe<*>, override val output: ItemLikeIngredient, val amount: Int) :
    NodeWithChildren {
    override val nodes: MutableList<StandardRecipeNode> = mutableListOf()

    val context = RecipeEvaluationContext()

    init {
        evaluateChildren(amount, context)
    }

}

@JvmInline
value class RecipeEvaluationContext(val map: MutableMap<String, Int> = mutableMapOf()) :
    MutableMap<String, Int> by map {

    operator fun get(ingredient: Ingredient): Int = this[ingredient.serialize()] ?: 0
    operator fun set(ingredient: Ingredient, value: Int) {
        this[ingredient.serialize()] = value
    }

}
