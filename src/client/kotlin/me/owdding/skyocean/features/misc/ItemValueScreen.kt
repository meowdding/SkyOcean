package me.owdding.skyocean.features.misc

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.BaseParentWidget
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LEFT
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.Displays.background
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.withPadding
import me.owdding.lib.extensions.shorten
import me.owdding.skyocean.mixins.FrameLayoutAccessor
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.asWidget
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent
import tech.thatgravyboat.skyblockapi.api.item.calculator.GroupedEntry
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemEntry
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemValueSource
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.max

class ItemValueScreen(val item: ItemStack) : SkyOceanScreen("Item Value") {

    val widgetWidth get() = (width / 3).coerceAtLeast(100) + 50
    val widgetHeight get() = (height / 3).coerceAtLeast(100) + 50

    override fun init() {
        val (raw, price, _, tree) = item.getItemValue()

        val width = widgetWidth
        val height = widgetHeight

        Displays.layered(
            background(olympus("buttons/normal"), width, height),
            background(olympus("buttons/dark/normal"), width, 26),
        ).asWidget().center().applyAsRenderable()


        LayoutFactory.frame(width, height) {
            vertical {
                vertical(alignment = LEFT) {
                    LayoutFactory.horizontal(5, MIDDLE) {
                        spacer(2, 24)
                        background(
                            olympus("textbox/normal"),
                            Displays.item(item, showTooltip = true).withPadding(2),
                        ).asWidget().add()
                        string(item.hoverName)
                        string(price.toFormattedString()) {
                            this.color = TextColor.GOLD
                        }
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
                        tree.filter { it.price > 0 }.forEach {
                            widget(it.asWidget() { callback() })
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
                            contents.setMinHeight(height)
                            (contents as? FrameLayoutAccessor)?.children()?.clear()
                            contents.addChild(widget, LayoutSettings.defaults().alignHorizontallyCenter().alignVerticallyMiddle())
                        }
                }
                callback.invoke()
                refreshCallback(callback)
            },
        )
    }

    private fun GroupedEntry.asWidget(callback: () -> Unit): LayoutElement {
        return when (this.source) {
            ItemValueSource.REFORGE -> {
                Widgets.text("Reforge: ${price.toFormattedString()}")
            }

            ItemValueSource.ENCHANTMENT -> {
                val enchants = this.entries.filterIsInstance<ItemEntry>()
                CLickToExpandWidget(
                    Widgets.text(
                        Text.of("${enchants.size} Enchantments: ") {
                            this.color = TextColor.DARK_GRAY
                            append(" ${price.shorten()}") {
                                this.color = TextColor.GOLD
                            }
                        },
                    ),
                    LayoutFactory.vertical {
                        enchants.filter { it.price > 0 }
                            .sortedByDescending { it.price }
                            .forEach {
                                val (name, level) = it.itemId.removePrefix("ENCHANTMENT_").let { str ->
                                    str.substringBeforeLast('_') to str.substringAfterLast('_').toInt()
                                }
                                val isUltimate = name.startsWith("ULTIMATE_", true)
                                string("") {
                                    append(name.toTitleCase() + " $level") {
                                        this.color = if (isUltimate) TextColor.PINK else TextColor.DARK_PURPLE
                                        this.bold = isUltimate
                                    }
                                    append(": ") {
                                        this.color = TextColor.DARK_GRAY
                                    }
                                    append(it.price.shorten()) {
                                        this.color = TextColor.GOLD
                                    }
                                }
                            }
                    },
                    callback,
                )
            }

            else -> {
                Widgets.text("${this.source.name}: ${price.shorten()}")
            }
        }
    }

    @Module
    companion object {
        val ITEM_VALUE_KEY: KeyMapping = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "skyocean.keybind.item_value",
                GLFW.GLFW_KEY_J,
                "skyocean",
            ),
        )

        @Subscription
        fun onKeypress(event: ScreenKeyReleasedEvent) {
            if (!ITEM_VALUE_KEY.matches(event.key, event.scanCode)) return
            val item = McScreen.asMenu?.getHoveredSlot()?.item?.takeUnless { it.isEmpty } ?: return
            McClient.setScreenAsync(ItemValueScreen(item))
        }
    }
}

class CLickToExpandWidget(title: LayoutElement, body: LayoutElement, val callback: () -> Unit, val bodyOffset: Int = 5) : BaseParentWidget() {
    val title = title.asWidget()
    val body = body.asWidget()
    var expanded = false

    init {
        this.addRenderableWidget(this.title)
        this.addRenderableWidget(this.body)
    }

    override fun getWidth() = if (expanded) max(body.width + bodyOffset, title.width) else title.width
    override fun getHeight() = title.height + if (expanded) body.height else 0

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (title.isMouseOver(mouseX, mouseY) && button == 0) {
            expanded = !expanded
            title.isFocused = expanded
            body.visible = expanded
            body.isFocused = expanded
            callback()
            return true
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        title.setPosition(this.x, this.y)
        title.render(graphics, mouseX, mouseY, partialTicks)
        if (expanded) {
            body.setPosition(this.x + bodyOffset, this.y + title.height)
            body.render(graphics, mouseX, mouseY, partialTicks)
        }

        super.renderWidget(graphics, mouseX, mouseY, partialTicks)
    }
}
