package me.owdding.skyocean.features.misc

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.BaseParentWidget
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LEFT
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.Displays.background
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.withPadding
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.asWidget
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemValueSource
import tech.thatgravyboat.skyblockapi.api.item.calculator.Pricing
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.max

class ItemValueScreen(val item: ItemStack) : SkyOceanScreen("Item Value") {

    val widgetWidth get() = (width / 3).coerceAtLeast(100) + 50
    val widgetHeight get() = (height / 3).coerceAtLeast(100) + 50

    override fun init() {
        val (raw, price, sources) = item.getItemValue()

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

                    LayoutFactory.vertical {
                        sources.filterValues { it > 0 }.forEach {
                            widget(it.key.asWidget(it.value))
                        }
                    }.asScrollable(width - 5, height = height - 29).add()
                }
            }
        }.center().applyLayout()
    }

    private fun ItemValueSource.asWidget(amount: Long): LayoutElement {
        return when (this) {
            ItemValueSource.REFORGE -> {
                Widgets.text("Reforge: ${amount.toFormattedString()}")
            }

            ItemValueSource.ENCHANTMENT -> {

                CLickToExpandWidget(
                    Widgets.text("Enchantment: ${amount.toFormattedString()}"),
                    LayoutFactory.vertical {
                        item.getData(DataTypes.ENCHANTMENTS)?.forEach {
                            val value = Pricing.getPrice("ENCHANTMENT_${it.key}_${it.value}")
                            string("${it.key} (${it.value}) ${value.toFormattedString()}") {
                                this.color = TextColor.PINK
                            }
                        }
                    },
                )
            }

            else -> {
                Widgets.text("${this.name}: ${amount.toFormattedString()}")
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

class CLickToExpandWidget(title: LayoutElement, body: LayoutElement) : BaseParentWidget() {
    val title = title.asWidget()
    val body = body.asWidget()
    var expanded = false

    init {
        this.addRenderableWidget(this.title)
        this.addRenderableWidget(this.body)
    }

    override fun getWidth() = if (expanded) max(body.width, title.width) else title.width
    override fun getHeight() = title.height + if (expanded) body.height else 0

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (title.isMouseOver(mouseX, mouseY) && button == 0) {
            expanded = !expanded
            title.isFocused = expanded
            body.visible = expanded
            body.isFocused = expanded
            return true
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        title.setPosition(this.x, this.y)
        title.render(graphics, mouseX, mouseY, partialTicks)
        if (expanded) {
            body.setPosition(this.x + title.height, this.y)
            body.render(graphics, mouseX, mouseY, partialTicks)
        }

        super.renderWidget(graphics, mouseX, mouseY, partialTicks)
    }
}
