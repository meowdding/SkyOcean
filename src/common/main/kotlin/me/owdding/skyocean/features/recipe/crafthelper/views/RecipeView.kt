package me.owdding.skyocean.features.recipe.crafthelper.views

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.lib.extensions.floor
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.recipe.CurrencyIngredient
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.RecipeType
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.NodeWithChildren
import me.owdding.skyocean.features.recipe.crafthelper.RecipeNode
import me.owdding.skyocean.features.recipe.crafthelper.StandardRecipeNode
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.eval.TrackedItem
import me.owdding.skyocean.features.recipe.serialize
import me.owdding.skyocean.features.recipe.serializeWithAmount
import me.owdding.skyocean.utils.Icons
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

fun interface RecipeView {

    fun format(
        tree: ContextAwareRecipeTree,
        itemTracker: ItemTracker,
        visitor: RecipeView,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) {
        val context = CraftHelperContext.create(tree, itemTracker)
        evaluateNode(context)
        visitor.create(context.toState(), widget, widgetConsumer)
    }

    fun evaluateNode(context: CraftHelperContext) {
        val state = context.toState()

        if (context.node is NodeWithChildren) {
            for (node in context.node.nodes.reversed()) {
                state.hasChildren = true

                val childContext = context.push(node)
                evaluateNode(childContext)
                val childState = childContext.toState()
                state.childStates.add(childState)
                childState.isLast = context.node.nodes.first() == node
                state.childrenDone = state.childrenDone && (childState.isDone() || childState.childrenDone)
            }
        } else {
            state.childrenDone = false
        }
    }

    fun create(state: CraftHelperState, widget: WidgetBuilder, widgetConsumer: (AbstractWidget) -> Unit)
}

data class CraftHelperState(
    val ingredient: Ingredient,
    val itemTracker: ItemTracker,
    var path: String,
    val recipeType: RecipeType,
    val recipeOutputAmount: Int,
    var usedItems: MutableList<TrackedItem> = mutableListOf(),
    var childrenDone: Boolean = true,
    var canSupercraft: Boolean = false,
    var hasChildren: Boolean = false,
    var required: Int = 0,
    var amount: Int = 0,
    var amountThroughParents: Int = 0,
    var amountCarryOver: Int = 0,
    var isLast: Boolean = false,
    var hasBeenInitialized: Boolean = false,
    var parent: CraftHelperState? = null,
    var childStates: MutableList<CraftHelperState> = mutableListOf(),
) {
    fun isDone(): Boolean = (amount + amountCarryOver) >= (required)

    fun collect(): List<CraftHelperState> = buildList {
        add(this@CraftHelperState)
        childStates.filterNot { it == this@CraftHelperState }.forEach { addAll(it.collect()) }
    }

    fun totalAmount() = amount + amountCarryOver + amountThroughParents

}

data class CraftHelperContext(
    val path: String,
    val tracker: ItemTracker,
    val node: StandardRecipeNode,
    val ingredient: Ingredient,
    val state: CraftHelperState = CraftHelperState(
        ingredient = ingredient,
        itemTracker = tracker,
        path = path,
        usedItems = mutableListOf(),
        childStates = mutableListOf(),
        recipeType = node.recipe?.recipeType ?: RecipeType.UNKNOWN,
        recipeOutputAmount = node.recipe?.output?.amount ?: 1,
    ),
    val parent: CraftHelperContext? = null,
) {
    companion object {
        fun create(recipe: ContextAwareRecipeTree, tracker: ItemTracker) = CraftHelperContext("root", tracker, recipe, recipe.outputWithAmount)
    }

    fun push(node: StandardRecipeNode): CraftHelperContext = CraftHelperContext(
        node = node,
        ingredient = node.outputWithAmount,
        path = path + node.output.serialize(),
        tracker = tracker,
        parent = this,
    )

    fun toState(): CraftHelperState = state.apply {
        if (!hasBeenInitialized) {
            val (amount, list) = amount()
            this.amountThroughParents = amountThroughParents()
            this.amount = amount - amountThroughParents
            this.usedItems = list
            this.required = getMax() - amountThroughParents
            this.amountCarryOver = getCarryOver()
            this.hasBeenInitialized = true
            this.parent = this@CraftHelperContext.parent?.state
            this.path = path
        }
    }

    fun getRequired() = when (node) {
        is RecipeNode -> node.requiredAmount
        else -> node.outputWithAmount.amount
    }

    fun amountThroughParents(): Int {
        val parent = parent ?: return 0

        val amount = (getRequired() * (parent.toState().totalAmount() / parent.getRequired().toFloat())).floor()

        return amount.coerceIn(0..getRequired())
    }

    fun getCarryOver() = (node as? RecipeNode)?.carriedOver ?: 0
    fun getRequiredCrafts() = (node as? RecipeNode)?.requiredCrafts ?: 0

    fun getMax() = node.outputWithAmount.amount

    private fun amount(): Pair<Int, MutableList<TrackedItem>> {
        if (parent == null && CraftHelperConfig.noRootItems) return 0 to mutableListOf()

        var amount = amountThroughParents()

        val list: MutableList<TrackedItem> = mutableListOf()
        when (ingredient) {
            is CurrencyIngredient -> amount += tracker.takeCurrency(ingredient.amount - amount, ingredient.currency)
            is ItemLikeIngredient -> list.addAll(tracker.takeN(ingredient, getRequired() - amount))
        }
        amount += list.sumOf { it.amount }

        return amount to list
    }
}

class WidgetBuilder(val refreshCallback: (save: Boolean) -> Unit) {
    companion object {
        val noOp = WidgetBuilder {}
    }

    fun createLine(state: CraftHelperState): AbstractWidget {
        val widget = Widgets.text(state.ingredient.serializeWithAmount())
        return widget
    }

    fun name(ingredient: Ingredient): Component = when (ingredient) {
        is CurrencyIngredient -> ingredient.displayName
        is ItemLikeIngredient -> ingredient.itemName
        else -> CommonComponents.EMPTY
    }

    fun text(text: Component): AbstractWidget = Widgets.text(text).withColor(MinecraftColors.WHITE)

    fun getIcons(sources: List<ItemSources>): Component = Text.of {
        this.color = TextColor.GRAY
        if (ItemSources.WARDROBE in sources) append(Icons.WARDROBE)
        if (ItemSources.VAULT in sources) append(Icons.VAULT)
        if (ItemSources.ACCESSORY_BAG in sources) append(Icons.ACCESSORIES)
        if (ItemSources.FORGE in sources) append(Icons.FORGE)
        if (ItemSources.CHEST in sources) append(Icons.CHESTS)
        if (ItemSources.RIFT in sources) append(Icons.RIFT) { this.color = TextColor.DARK_PURPLE }
        if (ItemSources.DRILL_UPGRADE in sources || ItemSources.ROD_UPGRADE in sources) append(Icons.ITEM_IN_ITEM)
    }

    fun reload() = refreshCallback(false)
}
