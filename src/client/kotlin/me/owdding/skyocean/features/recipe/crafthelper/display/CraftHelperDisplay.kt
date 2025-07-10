package me.owdding.skyocean.features.recipe.crafthelper.display

import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withPadding
import me.owdding.lib.layouts.BackgroundWidget
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen.asScrollable
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
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
                amount = 10
                Text.of("Cleared current recipe!").sendWithPrefix()
            }
            then("recipe", StringArgumentType.greedyString(), CombinedSuggestionProvider(RecipeIdSuggestionProvider, RecipeNameSuggestionProvider)) {
                callback {
                    val input = this.getArgument("recipe", String::class.java)
                    currentRecipe = RepoItemsAPI.getItemIdByName(input) ?: input
                    amount = 10
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

            var lines = 0
            LayoutFactory.vertical {
                val tree = ContextAwareRecipeTree(recipe, output, amount)
                TreeFormatter.format(tree, tracker, WidgetBuilder {}) {
                    lines++
                    widget(it)
                }
            }.let {
                widget(it.asScrollable(it.width + 10, McFont.height * 20.coerceAtMost(lines)))
            }
        }.asWidget().let {
            val background = BackgroundWidget(SkyOcean.minecraft("tooltip/background"), SkyOcean.minecraft("tooltip/frame"), widget = it, padding = 14)
            background.setPosition(10, (McScreen.self?.height?.div(2) ?: 0) - (it.height / 2))
            background.visitWidgets { event.screen.addRenderableWidget(background) }
        }
    }
}
