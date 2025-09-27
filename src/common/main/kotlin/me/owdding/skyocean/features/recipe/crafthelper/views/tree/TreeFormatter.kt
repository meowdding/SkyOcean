package me.owdding.skyocean.features.recipe.crafthelper.views.tree

import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import net.minecraft.client.gui.components.AbstractWidget

object TreeFormatter : RecipeView {
    override fun create(
        state: CraftHelperState,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) = append(state, widget, widgetConsumer)

    fun append(state: CraftHelperState, widget: WidgetBuilder, widgetConsumer: (AbstractWidget) -> Unit, depth: Int = 0, prefix: String = ""): Unit =
        context(state) {
            if (state.required == 0 && CraftHelperConfig.hideCompleted) {
                return
            }

            widgetConsumer(widget.listEntry(prefix))

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
