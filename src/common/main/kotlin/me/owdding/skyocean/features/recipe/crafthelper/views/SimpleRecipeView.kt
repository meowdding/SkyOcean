package me.owdding.skyocean.features.recipe.crafthelper.views

import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import net.minecraft.client.gui.components.AbstractWidget

class SimpleRecipeView(val stateVisitor: (CraftHelperState) -> Unit) : RecipeView {

    fun visit(
        tree: ContextAwareRecipeTree,
        itemTracker: ItemTracker,
    ) = format(tree, itemTracker, WidgetBuilder.noOp) {}

    override fun create(
        state: CraftHelperState,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) {
        stateVisitor(state)
    }
}
