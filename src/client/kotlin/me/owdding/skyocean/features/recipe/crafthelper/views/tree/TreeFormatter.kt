package me.owdding.skyocean.features.recipe.crafthelper.views.tree

import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import net.minecraft.client.gui.components.AbstractWidget
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append

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

        widgetConsumer(
            widget.text(
                Text.of {
                    append(prefix)
                    append(available)
                    append("/")
                    append(needed)

                    append(" ")
                    append(name)
                },
            ),
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
