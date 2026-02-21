package me.owdding.skyocean.features.recipe.crafthelper.display

import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.compat.REIRenderOverlayEvent
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asButtonLeft
import me.owdding.lib.displays.withPadding
import me.owdding.lib.layouts.BackgroundWidget
import me.owdding.lib.layouts.asWidget
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperManager
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.features.recipe.crafthelper.views.raw.RawFormatter
import me.owdding.skyocean.features.recipe.crafthelper.views.tree.TreeFormatter
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.chat.Icons
import me.owdding.skyocean.utils.extensions.asScrollable
import me.owdding.skyocean.utils.extensions.tryClear
import me.owdding.skyocean.utils.extensions.withoutTooltipDelay
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import me.owdding.skyocean.utils.setPosition
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.left
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.max

@LateInitModule
object CraftHelperDisplay : MeowddingLogger by SkyOcean.featureLogger() {

    private var craftHelperLayout: LayoutElement? = null

    private const val BACKGROUND_PADDING = 14

    @Subscription
    fun onScreenInit(event: ScreenInitializedEvent) {
        if (!CraftHelperConfig.enabled) return
        if (!LocationAPI.isOnSkyBlock) return

        val screen = event.screen as? AbstractContainerScreen<*> ?: return

        val layout = LayoutFactory.empty() as FrameLayout
        lateinit var callback: (save: Boolean) -> Unit

        // - CraftHelperConfig.margin * 2 (customizable left/right margin)
        // - BACKGROUND_PADDING * 2 (Background padding)
        val maxAvailableWidth = max(50, screen.left - (CraftHelperConfig.margin * 2) - (BACKGROUND_PADDING * 2))

        fun resetLayout() {
            layout.visitWidgets { event.widgets.remove(it) }
        }
        callback = callback@{ save ->
            val (tree, output) = CraftHelperStorage.data?.resolve(::resetLayout, CraftHelperManager::clear) ?: return@callback
            resetLayout()
            layout.tryClear()
            layout.addChild(visualize(tree, output, maxAvailableWidth) { callback })
            layout.arrangeElements()
            layout.setPosition(CraftHelperConfig.position.position(layout.width, layout.height))
            layout.visitWidgets { event.widgets.add(it) }
            this.craftHelperLayout = layout
            if (save) CraftHelperStorage.save()
        }
        callback(false)
    }

    @Subscription
    fun onREI(event: REIRenderOverlayEvent) {
        craftHelperLayout?.let {
            event.register(it.x, it.y, it.width, it.height)
        }
    }

    @Subscription(ContainerCloseEvent::class)
    fun onScreenClose() {
        craftHelperLayout = null
    }

    @Suppress("LongMethod")
    private fun visualize(tree: ContextAwareRecipeTree, output: ItemLikeIngredient, maxWidth: Int, callback: () -> ((save: Boolean) -> Unit)): AbstractWidget {
        val sources = ItemSources.craftHelperSources - CraftHelperConfig.disallowedSources.toSet()
        val tracker = ItemTracker(sources)
        val callback = callback()

        return LayoutFactory.vertical(2) {
            var maxLine = 0
            var lines = 0
            val body = LayoutFactory.vertical {
                val list = mutableListOf<AbstractWidget>()
                runCatching {
                    val formatter = when (CraftHelperConfig.formatter) {
                        CraftHelperFormat.RAW -> RawFormatter
                        CraftHelperFormat.TREE -> TreeFormatter
                    }

                    formatter.format(tree, tracker, WidgetBuilder(refreshCallback = callback)) {
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

            val contentWidth = minOf(maxLine, maxWidth)

            horizontal(5, MIDDLE) {
                val item = ExtraDisplays.inventoryBackground(1, 1, Displays.item(output.item, showTooltip = true).withPadding(2))
                display(item)
                vertical(alignment = MIDDLE) {
                    spacer(max(0, contentWidth - item.getWidth() - 10))
                    display(Displays.component(output.itemName))
                    horizontal {
                        widget(
                            Displays.component(
                                Text.of {
                                    append("-")
                                    this.color = if (CraftHelperStorage.canModifyCount) TextColor.RED else TextColor.GRAY
                                },
                            ).asButtonLeft {
                                if (!CraftHelperStorage.canModifyCount) return@asButtonLeft

                                val value = CraftHelperStorage.selectedAmount / (tree.amountPerCraft)
                                val newValue = if (McScreen.isShiftDown) {
                                    value - 10
                                } else {
                                    value - 1
                                }
                                CraftHelperStorage.setAmount(max(1, newValue) * tree.amountPerCraft)
                                callback(true)
                            }.withTooltip(
                                Text.multiline(
                                    "§eClick§r to decrease by §c1",
                                    "§eShift + Click§r to decrease by §c10",
                                ).apply { this.color = TextColor.GRAY },
                            ).withoutTooltipDelay(),
                        )
                        textDisplay(" ${CraftHelperStorage.selectedAmount} ", shadow = true) {
                            this.color = TextColor.DARK_GRAY
                        }
                        widget(
                            Displays.component(
                                Text.of {
                                    append("+")
                                    this.color = if (CraftHelperStorage.canModifyCount) TextColor.GREEN else TextColor.GRAY
                                },
                            ).asButtonLeft {
                                if (!CraftHelperStorage.canModifyCount) return@asButtonLeft
                                val value = CraftHelperStorage.selectedAmount / tree.amountPerCraft
                                val newValue = if (McScreen.isShiftDown) {
                                    value + 10
                                } else {
                                    value + 1
                                }
                                CraftHelperStorage.setAmount(newValue * tree.amountPerCraft)
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

            widget(body.asScrollable(contentWidth, McFont.height * 20.coerceAtMost(lines)))
        }.asWidget().let {
            val background = BackgroundWidget(
                SkyOcean.minecraft("tooltip/background"), SkyOcean.minecraft("tooltip/frame"),
                widget = it, padding = BACKGROUND_PADDING,
            )
            background.setPosition(CraftHelperConfig.margin, (McScreen.self?.height?.div(2) ?: 0) - (it.height / 2))
            background
        }
    }
}
