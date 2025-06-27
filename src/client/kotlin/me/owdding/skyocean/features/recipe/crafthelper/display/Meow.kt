package me.owdding.skyocean.features.recipe.crafthelper.display

import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.toRow
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.features.recipe.crafthelper.*
import me.owdding.skyocean.features.recipe.crafthelper.SimpleRecipeApi.getBestRecipe
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.features.recipe.crafthelper.views.tree.TreeFormatter
import me.owdding.skyocean.features.recipe.crafthelper.visitors.RecipeVisitor
import me.owdding.skyocean.utils.LateInitModule
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@LateInitModule
object Meow {

    @Subscription
    fun test(event: ScreenInitializedEvent) {
        if (event.screen !is AbstractContainerScreen<*>) return
        val tracker = ItemTracker()
        event.widgets.add(
            LayoutFactory.vertical {

                val recipe = getBestRecipe("MOLTEN_POWDER") ?: run {
                    Text.of("No recipe found for MOLTEN_POWDER!") { this.color = TextColor.RED }.send()
                    return@vertical
                }
                val output = RecipeVisitor.getOutput(recipe) ?: run {
                    Text.of("Recipe output is null!") { this.color = TextColor.RED }.send()
                    return@vertical
                }

                val tree = ContextAwareRecipeTree(recipe, output, 3)
                TreeFormatter.format(tree, tracker, WidgetBuilder {}, ::widget)

            }.asWidget(),
        )
    }

    fun StandardRecipeNode.format(tracker: ItemTracker): Display = when (this) {
        is RecipeNode -> {
            buildList {
                if (carriedOver >= 1) {
                    add(Displays.text("$carriedOver + "))
                }

                add(Displays.text(tracker.takeN(output, requiredAmount).sumOf { it.amount }.toString()))
                add(Displays.text("/$totalRequired "))
                if (output is ItemLikeIngredient) {
                    add(Displays.text(output.itemName))
                } else {
                    add(Displays.text(output.serialize()))
                }

                add(Displays.empty())
            }.toRow()
        }

        is LeafNode -> {
            buildList {
                val totalRequired = output.amount

                val amount = when (output) {
                    is ItemLikeIngredient -> tracker.takeN(output, output.amount).sumOf { it.amount }
                    is CoinIngredient -> tracker.takeCoins(output.amount)
                    else -> 0
                }

                add(Displays.text(amount.toString()))
                add(Displays.text("/$totalRequired "))
                if (output is ItemLikeIngredient) {
                    add(Displays.text(output.itemName))
                } else {
                    add(Displays.text(output.serialize()))
                }

            }.toRow()
        }

        is ContextAwareRecipeTree -> Displays.text(output.serializeWithAmount())
        else -> Displays.empty()
    }

}
