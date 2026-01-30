package me.owdding.skyocean.features.recipe.crafthelper.views.tree

import earth.terrarium.olympus.client.ui.UIIcons
import me.owdding.lib.MeowddingLib
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.Displays.background
import me.owdding.lib.displays.asButtonLeft
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import net.minecraft.client.gui.components.AbstractWidget
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

object TreeFormatter : RecipeView {
    override fun create(
        state: CraftHelperState,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) = append(state, widget, widgetConsumer)

    fun append(
        state: CraftHelperState,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
        depth: Int = 0,
        prefix: String = "",
    ): Unit = context(state) {
        if (state.required == 0 && CraftHelperConfig.hideCompleted) {
            return
        }

        val hiddenPaths = widget.recipeData.hiddenPaths
        val isHidden = hiddenPaths.contains(state.path)

        val chevron = Displays.supplied {
            val chevron = if (!isHidden) UIIcons.CHEVRON_DOWN else MeowddingLib.id("chevron_right")
            background(chevron, Displays.empty(10, 10), TextColor.DARK_GRAY)
        }.asButtonLeft {
            if (isHidden) {
                hiddenPaths.remove(state.path)
            } else {
                hiddenPaths.add(state.path)
            }

            widget.reload(true)
        }

        widgetConsumer(LayoutFactory.horizontal {
            widget(widget.listEntry(prefix))
            widget(chevron)
        }.asWidget())

        if (isHidden) {
            return
        }
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
