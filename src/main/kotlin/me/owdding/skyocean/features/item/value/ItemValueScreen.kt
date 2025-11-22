package me.owdding.skyocean.features.item.value

import com.mojang.blaze3d.platform.InputConstants
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.builder.LEFT
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.Displays.background
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.withPadding
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.features.item.value.SourceToWidget.asWidget
import me.owdding.skyocean.mixins.FrameLayoutAccessor
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.extensions.asScrollable
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class ItemValueScreen(val item: ItemStack) : SkyOceanScreen("Item Value") {

    val widgetWidth get() = (width / 3).coerceAtLeast(100) + 50
    val widgetHeight get() = (height / 3).coerceAtLeast(100) + 50

    override fun init() {
        val (_, price, tree) = item.getItemValue()

        val width = widgetWidth
        val height = widgetHeight

        Displays.layered(
            background(olympus("buttons/normal"), width, height),
            background(olympus("buttons/dark/normal"), width, 26),
        ).asWidget().center().applyAsRenderable()


        LayoutFactory.frame(width, height) {
            vertical {
                vertical(alignment = LEFT) {
                    LayoutFactory.horizontal(alignment = MIDDLE) {
                        val padding = 2
                        val icon = 20
                        val spacing = 5

                        spacer(padding, 24)
                        background(
                            olympus("textbox/normal"),
                            Displays.item(item, showTooltip = true).withPadding(2),
                        ).asWidget().add()
                        spacer(spacing)
                        val titleWidth = width - padding - icon - spacing - padding
                        display(
                            Displays.fixedWidth(
                                DisplayFactory.horizontal(5) {
                                    string(item.hoverName)
                                    string(price.toFormattedString()) {
                                        this.color = TextColor.GOLD
                                    }
                                },
                                titleWidth,
                            ),
                        )
                    }.add()
                    spacer(width)
                }
                spacer(height = 2)
                spacer(width = 4, height - 26)
            }
            horizontal {
                spacer(height = height)
                vertical {
                    spacer(width, 26)

                    lateinit var callback: () -> Unit

                    LayoutFactory.vertical {
                        spacer(height = 1)
                        tree.filter { it.price > 0 }.sortedByDescending { it.price }.forEach {
                            widget(it.asWidget { callback() })
                        }
                    }.asRefreshableScrollable(width - 5, height = widgetHeight - 29) { callback = it }.add()
                }
            }
        }.center().applyLayout()
    }

    fun Layout.asRefreshableScrollable(
        width: Int,
        height: Int,
        refreshCallback: (() -> Unit) -> Unit,
    ): LayoutElement {
        return LayoutWidget(this).also { it.visible = true }.withStretchToContentSize().asScrollable(
            width, height,
            {
                val callback: () -> Unit = {
                    this@asRefreshableScrollable.arrangeElements()
                    val widget = LayoutWidget(this@asRefreshableScrollable).also { it.visible = true }.withStretchToContentSize()
                    this.withSize(width, height)
                        .withContents { contents ->
                            contents.setMinWidth(width - 10)
                            (contents as? FrameLayoutAccessor)?.children()?.clear()
                            contents.addChild(widget, LayoutSettings.defaults().alignHorizontallyCenter().alignVerticallyMiddle())
                        }
                }
                callback.invoke()
                refreshCallback(callback)
            },
        )
    }

    @Module
    companion object {
        val ITEM_VALUE_KEY = SkyOceanKeybind("item_value", InputConstants.KEY_J)

        @Subscription
        fun onKeypress(event: ScreenKeyReleasedEvent) {
            if (!ITEM_VALUE_KEY.matches(event)) return
            val item = McScreen.asMenu?.getHoveredSlot()?.item?.takeUnless { it.isEmpty } ?: return
            McClient.setScreenAsync { ItemValueScreen(item) }
        }
    }
}
