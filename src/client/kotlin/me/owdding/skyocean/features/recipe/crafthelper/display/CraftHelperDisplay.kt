package me.owdding.skyocean.features.recipe.crafthelper.display

import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withPadding
import me.owdding.lib.layouts.BackgroundWidget
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen.asScrollable
import me.owdding.skyocean.features.recipe.crafthelper.*
import me.owdding.skyocean.features.recipe.crafthelper.SimpleRecipeApi.getBestRecipe
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.features.recipe.crafthelper.views.tree.TreeFormatter
import me.owdding.skyocean.features.recipe.crafthelper.visitors.RecipeVisitor
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import me.owdding.skyocean.utils.suggestions.CombinedSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeIdSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeNameSuggestionProvider
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@LateInitModule
object CraftHelperDisplay {

    var currentRecipe: String? = null
    var amount: Int = 1

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("recipe") {
            thenCallback("clear") {
                currentRecipe = null
                amount = 1
                Text.of("Cleared current recipe!").sendWithPrefix()
            }
            then("recipe", StringArgumentType.greedyString(), CombinedSuggestionProvider(RecipeIdSuggestionProvider, RecipeNameSuggestionProvider)) {
                callback {
                    val input = this.getArgument("recipe", String::class.java)
                    currentRecipe = RepoItemsAPI.getItemIdByName(input) ?: input
                    amount = 1
                    Text.of("Set current recipe to $currentRecipe!").sendWithPrefix()
                }
            }
        }
    }

    @Subscription
    fun onScreenInit(event: ScreenInitializedEvent) {
        if (event.screen !is AbstractContainerScreen<*>) return
        val currentRecipe = currentRecipe ?: return

        val recipe = getBestRecipe(currentRecipe) ?: run {
            Text.of("No recipe found for $currentRecipe!") { this.color = TextColor.RED }.sendWithPrefix()
            return
        }
        val output = RecipeVisitor.getOutput(recipe) ?: run {
            Text.of("Recipe output is null!") { this.color = TextColor.RED }.sendWithPrefix()
            return
        }

        val tracker = ItemTracker()
        LayoutFactory.vertical(2) {
            horizontal(5, MIDDLE) {
                display(ExtraDisplays.inventoryBackground(1, 1, Displays.item(output.item, showTooltip = true).withPadding(2)))
                string(output.itemName)
            }

            LayoutFactory.vertical {
                val tree = ContextAwareRecipeTree(recipe, output, amount)
                TreeFormatter.format(tree, tracker, WidgetBuilder {}, ::widget)
            }.let {
                widget(it.asScrollable(it.width + 10, McFont.height * 15))
            }
        }.asWidget().let {
            val background = BackgroundWidget(SkyOcean.minecraft("tooltip/background"), SkyOcean.minecraft("tooltip/frame"), widget = it, padding = 14)
            background.setPosition(10, (McScreen.self?.height?.div(2) ?: 0) - (it.height / 2))
            background.visitWidgets { event.screen.addRenderableWidget(background) }
        }
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
