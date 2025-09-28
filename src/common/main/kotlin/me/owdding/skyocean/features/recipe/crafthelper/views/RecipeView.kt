package me.owdding.skyocean.features.recipe.crafthelper.views

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.floor
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.features.item.sources.ForgeItemContext
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.recipe.CurrencyIngredient
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.RecipeType
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.NodeWithChildren
import me.owdding.skyocean.features.recipe.crafthelper.RecipeNode
import me.owdding.skyocean.features.recipe.crafthelper.StandardRecipeNode
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.eval.TrackedItem
import me.owdding.skyocean.features.recipe.serialize
import me.owdding.skyocean.features.recipe.serializeWithAmount
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.chat.ChatUtils.append
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.ComponentIcons
import me.owdding.skyocean.utils.chat.Icons
import me.owdding.skyocean.utils.extensions.withoutTooltipDelay
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.join
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.time.until

fun interface RecipeView {

    fun format(
        tree: ContextAwareRecipeTree,
        itemTracker: ItemTracker,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) {
        val context = CraftHelperContext.create(tree, itemTracker)
        evaluateNode(context)
        create(context.toState(), widget, widgetConsumer)
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
    companion object {
        fun merge(entries: List<CraftHelperState>) = CraftHelperState(
            entries.first().ingredient,
            entries.first().itemTracker,
            entries.first().ingredient.serializeWithAmount(),
            entries.first().recipeType,
            entries.sumOf { it.recipeOutputAmount },
            entries.flatMap { it.usedItems }.toMutableList(),
            entries.all { it.childrenDone },
            entries.all { it.canSupercraft },
            false,
            entries.sumOf { it.required },
            entries.sumOf { it.amount },
            entries.sumOf { it.amountThroughParents },
            entries.sumOf { it.amountCarryOver },
            false,
            entries.all { it.hasBeenInitialized },
            null,
        )
    }

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

    context(state: CraftHelperState)
    fun name(): Component = when (val ingredient = state.ingredient) {
        is CurrencyIngredient -> ingredient.displayName
        is ItemLikeIngredient -> ingredient.itemName
        else -> CommonComponents.EMPTY
    }

    fun text(text: Component): AbstractWidget = Widgets.text(text).withColor(MinecraftColors.WHITE)

    context(state: CraftHelperState)
    fun getIcons(sources: List<ItemSources> = state.usedItems.map { it.source }): Component = Text.of {
        this.color = TextColor.GRAY
        if (ItemSources.WARDROBE in sources) append(ComponentIcons.WARDROBE)
        if (ItemSources.VAULT in sources) append(Icons.VAULT)
        if (ItemSources.ACCESSORY_BAG in sources) append(ComponentIcons.ACCESSORIES)
        if (ItemSources.FORGE in sources) append(ComponentIcons.FORGE)
        if (ItemSources.CHEST in sources) append(ComponentIcons.CHESTS)
        if (ItemSources.RIFT in sources) append(Icons.RIFT) { this.color = TextColor.DARK_PURPLE }
        if (ItemSources.DRILL_UPGRADE in sources || ItemSources.ROD_UPGRADE in sources) append(ComponentIcons.ITEM_IN_ITEM)
        if (ItemSources.HUNTING_BOX in sources) append(ComponentIcons.BOX)
    }

    fun reload() = refreshCallback(false)

    context(state: CraftHelperState)
    fun tooltip() = buildList<Component> {
        val sources = state.usedItems.groupBy { it.source }
        fun addUsedSources() {
            if (isNotEmpty()) return
            add(!"Used Item Sources:")
            add(CommonComponents.EMPTY)
        }

        fun addSimple(source: ItemSources, name: Component) {
            if (!sources.containsKey(source)) return
            addUsedSources()
            if (state.amountCarryOver != 0 || state.amountThroughParents != 0) {
                add(CommonComponents.EMPTY)
            }
            add(
                Text.of {
                    append(name)
                    append(": ")
                    append(sources.getValue(source).sumOf { it.amount }.toFormattedString())
                },
            )
        }

        if (state.amountThroughParents != 0) {
            addUsedSources()
            add(!"Amount through parents: ${state.amountThroughParents.toFormattedString()}")
        }
        if (state.amountCarryOver != 0) {
            addUsedSources()
            add(!"Carry over from previous recipe: ${state.amountCarryOver.toFormattedString()}")
        }

        addSimple(ItemSources.INVENTORY, !"Inventory")
        addSimple(ItemSources.SACKS, !"Sacks")
        addSimple(ItemSources.STORAGE, !"Storage")
        addSimple(
            ItemSources.WARDROBE,
            join(ComponentIcons.WARDROBE, " Wardrobe"),
        )
        addSimple(
            ItemSources.CHEST,
            join(ComponentIcons.CHESTS, " Chest"),
        )
        addSimple(
            ItemSources.ACCESSORY_BAG,
            join(ComponentIcons.ACCESSORIES, " Accessory Bag"),
        )
        addSimple(ItemSources.VAULT, !"${Icons.VAULT} Vault")
        addSimple(ItemSources.RIFT, !"${Icons.RIFT} Rift")
        addSimple(
            ItemSources.DRILL_UPGRADE,
            join(ComponentIcons.ITEM_IN_ITEM, " Drill Upgrade"),
        )
        addSimple(
            ItemSources.ROD_UPGRADE,
            join(ComponentIcons.ITEM_IN_ITEM, "Rod Upgrade"),
        )
        addSimple(
            ItemSources.HUNTING_BOX,
            join(ComponentIcons.BOX, " Hunting Box"),
        )

        if (sources.containsKey(ItemSources.FORGE)) {
            addUsedSources()
            sources.getValue(ItemSources.FORGE).map { it.context }.filterIsInstance<ForgeItemContext>().forEach { context ->
                val time = context.finishTime.until()
                val timeDisplay = if (time.isNegative()) "Done" else time.toReadableTime()

                add(!"${Icons.FORGE} Forge Slot: ${context.slot} - $timeDisplay")
            }
        }

        if (idOrNull() != null && state.recipeType != RecipeType.UNKNOWN) {
            if (this.isNotEmpty()) add(CommonComponents.EMPTY)
            add(!"§eClick to open recipe!")
        }
    }.takeUnless { it.isEmpty() }?.toMutableList()?.let {
        Tooltip.create(
            Text.multiline(it) {
                this.color = TextColor.GRAY
            },
        )
    }

    context(state: CraftHelperState)
    fun idOrNull() = (state.ingredient as? SkyOceanItemIngredient)?.skyblockId

    context(state: CraftHelperState)
    fun text(prefix: String = "") = Displays.component(
        Text.of {
            val parentAmount = if (CraftHelperConfig.parentAmount) {
                state.amountThroughParents
            } else 0

            val needed = (state.required + parentAmount)
            val available = state.amount + parentAmount
            append(prefix) { this.color = TextColor.DARK_GRAY }
            append {
                when {
                    state.isDone() -> {
                        append(Icons.CHECKMARK)
                        this.color = TextColor.GREEN
                        this.bold = true
                    }

                    state.childrenDone -> {
                        append(Icons.WARNING)
                        this.color = TextColor.YELLOW
                    }

                    else -> {
                        append(Icons.CROSS)
                        this.color = TextColor.RED
                        this.bold = true
                    }
                }
                append(" ")
            }
            append {
                append(available.toFormattedString())
                append("/") { color = TextColor.GRAY }
                append(needed.toFormattedString())

                this.color = ARGB.lerp(available.toFloat() / needed.toFloat(), TextColor.RED, TextColor.GREEN)
            }

            append(" ")
            append(this@WidgetBuilder.name())
            append(" ")
            append(this@WidgetBuilder.getIcons())
        },
    )

    context(state: CraftHelperState)
    fun listEntry(prefix: String = ""): Button = Widgets.button {
        val tooltip = this.tooltip()
        val text = this.text(prefix)
        it.withTexture(null)
        it.withSize(text.getWidth(), text.getHeight())
        it.withRenderer(DisplayWidget.displayRenderer(text))
        it.withCallback {
            val id = this.idOrNull() ?: return@withCallback
            when (state.recipeType) {
                RecipeType.CUSTOM -> SkyOcean.debug("Custom recipes dont support click actions!")
                RecipeType.UNKNOWN -> SkyOcean.debug("Clicked unknown recipe type for $id")
                RecipeType.KAT -> Text.of("No preview yet, go to Kat :(").sendWithPrefix()
                else if state.recipeType.command != null -> McClient.sendClientCommand("${state.recipeType.command} $id")
                else -> SkyOcean.debug("Clicked recipe type with undefined click behaviour ($id)")
            }
        }
        it.setTooltip(tooltip)
    }.withoutTooltipDelay()

}
