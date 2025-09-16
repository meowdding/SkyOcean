package me.owdding.skyocean.features.inventory.buttons

import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.base.renderer.WidgetRendererContext
import earth.terrarium.olympus.client.components.buttons.Button
import me.owdding.skyocean.SkyOcean.minecraft
import me.owdding.skyocean.config.features.inventory.ButtonConfig
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.platform.pushPop
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

class InvButton(
    val button: ButtonConfig,
    val rowIndex: Int,
    val bottom: Boolean,
    val screen: Screen,
    val index: Int,
    val baseX: Int,
    val baseY: Int,
    val baseWidth: Int,
    val baseHeight: Int,
) : Button() {
    var highlight = false

    fun renderButtons(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        graphics.pushPop {
            val sprite = if (bottom) {
                SELECTED_BOTTOM_TABS[rowIndex]
            } else {
                SELECTED_TOP_TABS[rowIndex]
            }
            renderPrevious(graphics, mouseX, mouseY, partialTicks, sprite)

        }
    }

    fun renderItem(graphics: GuiGraphics) {
        val itemX = baseWidth / 2 - 8 + this@InvButton.x
        val itemY = if (bottom) {
            baseHeight + this@InvButton.y - (baseWidth / 2) - 8
        } else {
            baseWidth / 2 - 8 + this@InvButton.y
        }
        graphics.renderItem(button.itemStack, itemX, itemY)

    }

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.isHovered = graphics.containsPointInScissor(mouseX, mouseY) && isMouseOver(mouseX.toDouble(), mouseY.toDouble())
        this.highlight = screen.title.stripped.trim().matches(button.regex) || (screen is ButtonConfigScreen && screen.selectedButtonIndex == this.index)

        val modifier = if (bottom) 4 else -4
        if (this.isHoveredOrFocused || this.highlight) {
            this.setPosition(baseX, baseY + modifier)
            this.withShape { x, y, width, height -> x >= 0 && x < width && y >= 0 && y < height }
        } else {
            this.setPosition(baseX, baseY)
            this.withShape { x, y, width, height ->
                if (bottom) {
                    x in 0.0..<width.toDouble() && y in 4.0..<height.toDouble()
                } else {
                    x in 0.0..<width.toDouble() && y in 0.0..<(height - 4.0)
                }
            }
        }
        return
    }

    fun renderPrevious(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float, sprite: ResourceLocation) {
        graphics.drawSprite(
            sprite,
            this.x,
            this.y,
            baseWidth,
            baseHeight,
        )

        WidgetRenderer.empty<Button>().render(graphics, WidgetRendererContext(this, mouseX, mouseY), partialTick)
    }

    companion object {
        val SELECTED_TOP_TABS = Array(7) { minecraft("container/creative_inventory/tab_top_selected_${it + 1}") }
        val SELECTED_BOTTOM_TABS = Array(7) { minecraft("container/creative_inventory/tab_bottom_selected_${it + 1}") }
        val UNSELECTED_TOP_TABS = Array(7) { minecraft("container/creative_inventory/tab_top_unselected_${it + 1}") }
        val UNSELECTED_BOTTOM_TABS = Array(7) { minecraft("container/creative_inventory/tab_bottom_unselected_${it + 1}") }
    }
}
