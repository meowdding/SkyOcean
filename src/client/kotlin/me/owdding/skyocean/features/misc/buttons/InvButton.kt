package me.owdding.skyocean.features.misc.buttons

import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.base.renderer.WidgetRendererContext
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.buttons.ButtonShapes
import me.owdding.skyocean.config.features.misc.ButtonConfig
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import kotlin.jvm.optionals.getOrNull

class InvButton(val button: ButtonConfig, val rowIndex: Int, val bottom: Boolean, val screen: Screen, val index: Int, val baseX: Int, val baseY: Int, val baseWidth: Int, val baseHeight: Int) : Button() {
    var highlight = false
    fun renderButtons(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        graphics.pushPop {
            val sprite = if (bottom) {
                SELECTED_BOTTOM_TABS[rowIndex]
            } else {
                SELECTED_TOP_TABS[rowIndex]
            }
            renderPrevious(graphics, mouseX, mouseY, partialTicks, sprite)
            val itemX = baseWidth / 2 - 8 + this@InvButton.x
            val itemY = if (bottom) {
                baseHeight  + this@InvButton.y - (baseWidth / 2) - 8
            } else {
                baseWidth / 2 - 8 + this@InvButton.y
            }
            val stack = if (button.item.contains(":")) {
                BuiltInRegistries.ITEM.get(ResourceLocation.bySeparator(button.item, ':'))?.getOrNull()?.value()?.defaultInstance ?: Items.BARRIER.defaultInstance
            } else {
                RepoItemsAPI.getItem(button.item)
            }
            graphics.renderItem(stack, itemX, itemY)
        }
    }
    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.highlight = screen.title.stripped.matches(Regex(button.title)) || (screen is ButtonConfigScreen && screen.selectedButtonIndex == this.index)
        val modifier = if (bottom) 4 else -4
        if (this.isHoveredOrFocused || this.highlight) {
            this.setPosition(baseX, baseY + modifier)
            this.withShape { x, y, width, height -> x >= 0 && x < width && y >= 0 && y < height }
        } else {
            this.setPosition(baseX, baseY)
            this.withShape { x, y, width, height ->
                if (bottom) {
                    return@withShape x >= 0 && x < width && y >= 4 && y < height
                } else {
                    return@withShape x >= 0 && x < width && y >= 0 && y < (height - 4)
                }
            }
        }
        return
    }

    fun renderPrevious(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float, sprite: ResourceLocation) {
        graphics.blitSprite(
            RenderType::guiOpaqueTexturedBackground,
            sprite,
            this.x, this.y,
            baseWidth, baseHeight,
            -1
        )

        WidgetRenderer.empty<Button>().render(graphics, WidgetRendererContext(this, mouseX, mouseY), partialTick)
    }

    companion object {
        val UNSELECTED_TOP_TABS = arrayOf<ResourceLocation>(
            mcrl("container/creative_inventory/tab_top_unselected_1"),
            mcrl("container/creative_inventory/tab_top_unselected_2"),
            mcrl("container/creative_inventory/tab_top_unselected_3"),
            mcrl("container/creative_inventory/tab_top_unselected_4"),
            mcrl("container/creative_inventory/tab_top_unselected_5"),
            mcrl("container/creative_inventory/tab_top_unselected_6"),
            mcrl("container/creative_inventory/tab_top_unselected_7"),
        )
        val SELECTED_TOP_TABS = arrayOf<ResourceLocation>(
            mcrl("container/creative_inventory/tab_top_selected_1"),
            mcrl("container/creative_inventory/tab_top_selected_2"),
            mcrl("container/creative_inventory/tab_top_selected_3"),
            mcrl("container/creative_inventory/tab_top_selected_4"),
            mcrl("container/creative_inventory/tab_top_selected_5"),
            mcrl("container/creative_inventory/tab_top_selected_6"),
            mcrl("container/creative_inventory/tab_top_selected_7"),
        )
        val UNSELECTED_BOTTOM_TABS = arrayOf<ResourceLocation>(
            mcrl("container/creative_inventory/tab_bottom_unselected_1"),
            mcrl("container/creative_inventory/tab_bottom_unselected_2"),
            mcrl("container/creative_inventory/tab_bottom_unselected_3"),
            mcrl("container/creative_inventory/tab_bottom_unselected_4"),
            mcrl("container/creative_inventory/tab_bottom_unselected_5"),
            mcrl("container/creative_inventory/tab_bottom_unselected_6"),
            mcrl("container/creative_inventory/tab_bottom_unselected_7"),
        )
        val SELECTED_BOTTOM_TABS = arrayOf<ResourceLocation>(
            mcrl("container/creative_inventory/tab_bottom_selected_1"),
            mcrl("container/creative_inventory/tab_bottom_selected_2"),
            mcrl("container/creative_inventory/tab_bottom_selected_3"),
            mcrl("container/creative_inventory/tab_bottom_selected_4"),
            mcrl("container/creative_inventory/tab_bottom_selected_5"),
            mcrl("container/creative_inventory/tab_bottom_selected_6"),
            mcrl("container/creative_inventory/tab_bottom_selected_7"),
        )
    }
}

private fun mcrl(location: String): ResourceLocation = ResourceLocation.withDefaultNamespace(location)
