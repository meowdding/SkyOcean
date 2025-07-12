package me.owdding.skyocean.features.recipe.crafthelper.views.raw

import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.serialize
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import net.minecraft.client.gui.components.AbstractWidget
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append

object RawFormatter : RecipeView {
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
    ) {
        val childLess = state.collect().filterNot { it.hasChildren }.groupBy { it.ingredient.serialize() }.values

        childLess.forEach {
            val needed = it.sumOf { it.required + it.amountThroughParents }
            val available = it.sumOf { it.amount + it.amountThroughParents }

            val name = widget.name(it.first().ingredient)

            widgetConsumer(
                widget.text(
                    Text.of {
                        append(available)
                        append("/")
                        append(needed)

                        append(" ")
                        append(name)
                    },
                ),
            )
        }
    }
}
