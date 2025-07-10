package me.owdding.skyocean.features.recipe.crafthelper.views.tree

import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.utils.ChatUtils
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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
        val needed = state.required + state.amountThroughParents
        val available = state.amount + state.amountThroughParents

        val name = widget.name(state.ingredient)
        if (state.amountThroughParents == needed) {
            return
        }

        widgetConsumer(
            widget.text(
                Text.of {
                    append(prefix) { this.color = TextColor.DARK_GRAY }
                    append("") {
                        when {
                            state.isDone() -> {
                                append(ChatUtils.CHECKMARK)
                                this.color = TextColor.GREEN
                                this.bold = true
                            }

                            state.childrenDone -> {
                                append(ChatUtils.WARNING)
                                this.color = TextColor.YELLOW
                            }

                            else -> {
                                append(ChatUtils.CROSS)
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
                },
            ).apply {
                if (state.usedItems.isEmpty()) return@apply
                this.setTooltipDelay(0.seconds.toJavaDuration())
                this.tooltip = Tooltip.create(
                    Text.of {
                        state.usedItems.forEach {
                            append("${it.source} ${it.amount}\n")
                        }
                        append("Through parents: ${state.amountThroughParents}")
                    },
                )
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
