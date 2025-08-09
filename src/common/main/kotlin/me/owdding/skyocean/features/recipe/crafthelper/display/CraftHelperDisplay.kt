package me.owdding.skyocean.features.recipe.crafthelper.display

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asButtonLeft
import me.owdding.lib.displays.withPadding
import me.owdding.lib.layouts.BackgroundWidget
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen.asScrollable
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen.withoutTooltipDelay
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.SimpleRecipeApi.getBestRecipe
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.features.recipe.crafthelper.views.tree.TreeFormatter
import me.owdding.skyocean.mixins.FrameLayoutAccessor
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.Icons
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import me.owdding.skyocean.utils.suggestions.CombinedSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeIdSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeNameSuggestionProvider
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@LateInitModule
object CraftHelperDisplay {

    val data get() = CraftHelperStorage.data

    fun clear() {
        data?.item = null
        data?.amount = 1
        CraftHelperStorage.save()
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("recipe") {
            thenCallback("clear") {
                clear()
                Text.of("Cleared current recipe!").sendWithPrefix()
            }
            then("amount", IntegerArgumentType.integer()) {
                callback {
                    val amount = this.getArgument("amount", Int::class.java)
                    if (amount <= 0) {
                        Text.of("Amount must be greater than 0!").withColor(TextColor.RED).sendWithPrefix()
                        return@callback
                    }
                    data?.amount = amount
                    CraftHelperStorage.save()
                    Text.of("Set current recipe amount to ") {
                        append("$amount") { color = TextColor.GREEN }
                        append("!").sendWithPrefix()
                    }
                }
            }
            then("recipe", StringArgumentType.greedyString(), CombinedSuggestionProvider(RecipeIdSuggestionProvider, RecipeNameSuggestionProvider)) {
                callback {
                    val input = this.getArgument("recipe", String::class.java)
                    data?.amount = 1
                    data?.item = SkyOceanItemId.fromName(input, dropLast = false) ?: SkyOceanItemId.unknownType(input) ?: run {
                        val splitName = input.substringBeforeLast(" ")
                        val amount = input.substringAfterLast(" ").toIntOrNull() ?: 1
                        data?.amount = amount
                        SkyOceanItemId.fromName(splitName) ?: SkyOceanItemId.unknownType(splitName)
                    }
                    CraftHelperStorage.save()
                    Text.of("Set current recipe to ") {
                        append("${data?.amount ?: 1}x ") { color = TextColor.GREEN }
                        append(data?.item?.toItem()?.let(ItemStack::getHoverName) ?: !"unknown")
                        append("!")
                    }.sendWithPrefix()
                }
            }
        }
    }

    @Subscription
    fun onScreenInit(event: ScreenInitializedEvent) {
        if (!MiscConfig.craftHelperEnabled) return
        if (!LocationAPI.isOnSkyBlock) return
        if (event.screen !is AbstractContainerScreen<*>) return

        val layout = LayoutFactory.empty() as FrameLayout
        lateinit var callback: (save: Boolean) -> Unit

        fun resetLayout() {
            layout.visitWidgets { event.widgets.remove(it) }
        }
        callback = callback@{ save ->
            val currentRecipe = data?.item ?: run {
                resetLayout()
                return@callback
            }

            val recipe = getBestRecipe(currentRecipe) ?: run {
                Text.of("No recipe found for $currentRecipe!") { this.color = TextColor.RED }.sendWithPrefix()
                resetLayout()
                clear()
                return@callback
            }
            val output = recipe.output ?: run {
                Text.of("Recipe output is null!") { this.color = TextColor.RED }.sendWithPrefix()
                resetLayout()
                clear()
                return@callback
            }

            resetLayout()
            (layout as? FrameLayoutAccessor)?.children()?.clear()
            val tree = ContextAwareRecipeTree(recipe, output, data?.amount?.coerceAtLeast(1) ?: 1)
            layout.addChild(visualize(tree, output) { callback })
            layout.arrangeElements()
            layout.setPosition(10, (McScreen.self?.height?.div(2) ?: 0) - (layout.height / 2))
            layout.visitWidgets { event.widgets.add(it) }
            if (save) CraftHelperStorage.save()
        }
        callback(false)
    }

    private fun visualize(tree: ContextAwareRecipeTree, output: ItemLikeIngredient, callback: () -> ((save: Boolean) -> Unit)): AbstractWidget {
        val tracker = ItemTracker()
        val callback = callback()

        return LayoutFactory.vertical(2) {
            var maxLine = 0
            var lines = 0
            val body = LayoutFactory.vertical {
                val list = mutableListOf<AbstractWidget>()
                runCatching {
                    TreeFormatter.format(tree, tracker, WidgetBuilder(callback)) {
                        lines++
                        maxLine = maxOf(maxLine, it.width + 10)
                        list.add(it)
                    }
                }.onSuccess { list.forEach(::widget) }
                    .onFailure {
                        lines = 2
                        textDisplay {
                            append("An error occurred while displaying the recipe!")
                            this.color = TextColor.RED
                        }
                        textDisplay {
                            append("Error: ${it.message}")
                            this.color = TextColor.RED
                        }
                    }
            }.apply { visitChildren { child -> maxLine = maxOf(maxLine, child.width + 10) } }

            horizontal(5, MIDDLE) {
                val item = ExtraDisplays.inventoryBackground(1, 1, Displays.item(output.item, showTooltip = true).withPadding(2))
                display(item)
                vertical(alignment = MIDDLE) {
                    spacer(maxLine - item.getWidth() - 10)
                    display(Displays.component(output.itemName))
                    horizontal {
                        widget(
                            Displays.component(
                                Text.of {
                                    append("-")
                                    this.color = TextColor.RED
                                },
                            ).asButtonLeft {
                                val value = data?.amount ?: 1
                                val newValue = if (Screen.hasShiftDown()) {
                                    value - 10
                                } else {
                                    value - 1
                                }
                                data?.amount = maxOf(1, newValue)
                                callback(true)
                            }.withTooltip(
                                Text.multiline(
                                    "§eClick§r to decrease by §c1",
                                    "§eShift + Click§r to decrease by §c10",
                                ).apply { this.color = TextColor.GRAY },
                            ).withoutTooltipDelay(),
                        )
                        textDisplay(" ${data?.amount ?: 1} ", shadow = true) {
                            this.color = TextColor.DARK_GRAY
                        }
                        widget(
                            Displays.component(
                                Text.of {
                                    append("+")
                                    this.color = TextColor.GREEN
                                },
                            ).asButtonLeft {
                                val value = data?.amount ?: 1
                                val newValue = if (Screen.hasShiftDown()) {
                                    value + 10
                                } else {
                                    value + 1
                                }
                                data?.amount = newValue
                                callback(true)
                            }.withTooltip(
                                Text.multiline(
                                    "§eClick§r to increase by §a1",
                                    "§eShift + Click§r to increase by §a10",
                                ).apply { this.color = TextColor.GRAY },
                            ).withoutTooltipDelay(),
                        )
                    }
                }
                vertical(alignment = MIDDLE) {
                    widget(
                        Displays.component(Text.of(Icons.CROSS) { this.color = TextColor.RED }).asButtonLeft {
                            CraftHelperStorage.setSelected(null)
                            callback(false)
                        }.withoutTooltipDelay().withTooltip(Text.of("Close") { this.color = TextColor.RED }),
                    )
                    string("")
                }
            }

            body.let {
                widget(it.asScrollable(it.width + 10, McFont.height * 20.coerceAtMost(lines)))
            }
        }.asWidget().let {
            val background = BackgroundWidget(SkyOcean.minecraft("tooltip/background"), SkyOcean.minecraft("tooltip/frame"), widget = it, padding = 14)
            background.setPosition(10, (McScreen.self?.height?.div(2) ?: 0) - (it.height / 2))
            background
        }
    }
}
