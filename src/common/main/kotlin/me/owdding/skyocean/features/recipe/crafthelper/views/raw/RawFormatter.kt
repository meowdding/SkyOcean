package me.owdding.skyocean.features.recipe.crafthelper.views.raw

import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.features.recipe.serialize
import net.minecraft.client.gui.components.AbstractWidget

object RawFormatter : RecipeView {
    override fun create(
        state: CraftHelperState,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) {
        val childLess = state.collect().filterNot { it.hasChildren }.groupBy { it.ingredient.serialize() }.values

        childLess.map(CraftHelperState::merge).forEachIndexed { index, state ->
            context(state) {
                widgetConsumer(
                    widget.listEntry(if (index < childLess.size - 1) "├ " else "└ "),
                )
            }
        }
    }
}
