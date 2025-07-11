package me.owdding.skyocean.features.recipe.crafthelper.views.tree

import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.features.item.soures.ForgeItemContext
import me.owdding.skyocean.features.item.soures.ItemSources
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.utils.Icons
import me.owdding.skyocean.utils.Utils.not
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.time.until
import kotlin.time.Duration.Companion.seconds

object TreeFormatter : RecipeView {
    fun format(
        tree: ContextAwareRecipeTree,
        itemTracker: ItemTracker,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) = format(tree, itemTracker, this, widget, widgetConsumer)

    override fun create(
        state: CraftHelperState,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) = append(state, widget, widgetConsumer)

    fun append(state: CraftHelperState, widget: WidgetBuilder, widgetConsumer: (AbstractWidget) -> Unit, depth: Int = 0, prefix: String = "") {
        val parentAmount = if (MiscConfig.craftHelperParentAmount) {
            state.amountThroughParents
        } else 0

        val needed = state.required + parentAmount
        val available = state.amount + parentAmount

        val name = widget.name(state.ingredient)
        if (state.required == 0 && MiscConfig.craftHelperHideCompleted) {
            return
        }

        widgetConsumer(
            widget.text(
                Text.of {
                    append(prefix) { this.color = TextColor.DARK_GRAY }
                    append("") {
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
                    append("") {

                        append(available.toFormattedString())
                        append("/")
                        append(needed.toFormattedString())

                        this.color = ARGB.lerp(available.toFloat() / needed.toFloat(), TextColor.RED, TextColor.GREEN)
                    }

                    append(" ")
                    append(name)
                    append(" ")
                    append(widget.getIcons(state.usedItems.map { it.source }))
                },
            ).apply {
                buildList<Component> {

                    val sources = state.usedItems.groupBy { it.source }

                    var isFirst = true
                    fun addUsedSources() {
                        if (!isFirst) return
                        isFirst = false
                        add(!"Used Item Sources:")
                        add(CommonComponents.EMPTY)
                    }

                    fun addSimple(source: ItemSources, name: String) {
                        if (!sources.containsKey(source)) return
                        addUsedSources()
                        if (state.amountCarryOver != 0 || state.amountThroughParents != 0) {
                            add(CommonComponents.EMPTY)
                        }
                        add(
                            Text.of(name) {
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

                    addSimple(ItemSources.INVENTORY, "Inventory")
                    addSimple(ItemSources.SACKS, "Sacks")
                    addSimple(ItemSources.STORAGE, "Storage")
                    addSimple(ItemSources.WARDROBE, "${Icons.WARDROBE} Wardrobe")
                    addSimple(ItemSources.CHEST, "${Icons.CHESTS} Chest")
                    addSimple(ItemSources.ACCESSORY_BAG, "${Icons.ACCESSORIES} Accessory Bag")
                    addSimple(ItemSources.VAULT, "${Icons.VAULT} Vault")


                    if (sources.containsKey(ItemSources.FORGE)) {
                        addUsedSources()
                        sources.getValue(ItemSources.FORGE).map { it.context }.filterIsInstance<ForgeItemContext>().forEach { context ->
                            val time = context.finishTime.until()
                            val timeDisplay = if (time <= 0.seconds) "Done" else time.toReadableTime()

                            add(!"${Icons.FORGE} Forge Slot: ${context.slot} - $timeDisplay")
                        }
                    }
                }.takeUnless { it.isEmpty() }?.let {
                    this.tooltip = Tooltip.create(
                        Text.multiline(it) {
                            this.color = TextColor.GRAY
                        },
                    )
                }
            },
        )

        if (state.hasChildren) {
            state.childStates.forEach { childState ->
                append(childState, widget, widgetConsumer, depth + 1, alterPrefix(prefix, childState.isLast))
            }
        }
    }

    fun alterPrefix(prefix: String, isLast: Boolean) = if (prefix.isEmpty()) {
        if (isLast) "└ " else "├ "
    } else {
        prefix.replace("├", "│").replace("└", "  ") + (if (isLast) "└ " else "├ ")
    }
}
