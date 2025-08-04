package me.owdding.skyocean.helpers

import me.owdding.skyocean.helpers.ClientSideInventory.Slot.Companion.asSlots
import me.owdding.skyocean.utils.rendering.RenderUtils
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.platform.drawString
import tech.thatgravyboat.skyblockapi.platform.drawTexture
import tech.thatgravyboat.skyblockapi.platform.showTooltip
import tech.thatgravyboat.skyblockapi.utils.extentions.translated
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

abstract class ClientSideInventory(val titleComponent: String?, val rows: Int) : Screen(titleComponent?.let { Text.of(it) }) {
    val backgroundHeight = 114 + rows * 18
    val backgroundWidth = 176
    val x get() = (this.width - backgroundWidth) / 2
    val y get() = (this.height - backgroundHeight) / 2
    var slots: MutableList<Slot> = mutableListOf()
    val renderBackground = true

    fun addItems(items: List<ItemStack>) {
        slots = items.asSlots().toMutableList()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        if (titleComponent != null) {
            guiGraphics.drawString(Text.of(titleComponent) { color = TextColor.DARK_GRAY }, this.x + 8, this.y + 6, -1, false)
        }
        val offsetX = x + 8
        val offsetY = y + 18
        guiGraphics.translated(offsetX, offsetY) {
            slots.forEach {
                renderSlot(guiGraphics, it, mouseX - offsetX, mouseY - offsetY)
            }

            Slot.playerInventoryAsSlots(rows).forEach {
                renderSlot(guiGraphics, it, mouseX - offsetX, mouseY - offsetY)
            }
        }
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick)

        if (renderBackground) {
            renderMenuBackground(guiGraphics)
        }
        val textureX = (this.width - backgroundWidth) / 2
        val textureY = (this.height - backgroundHeight) / 2

        guiGraphics.drawTexture(
            TEXTURE,
            textureX,
            textureY,
            width = backgroundWidth,
            height = this.rows * 18 + 17,

            u1 = backgroundWidth / 256f,
            v1 = (this.rows * 18f + 17f) / 256f,
        )
        guiGraphics.drawTexture(
            TEXTURE,
            x = textureX, y = textureY + this.rows * 18 + 17,
            width = backgroundWidth, height = 96,

            u0 = 0f,
            u1 = backgroundWidth / 256f,
            v0 = 125f / 256f,
            v1 = backgroundHeight / 256f,
        )
    }

    private fun renderSlot(graphics: GuiGraphics, slot: Slot, mouseX: Int, mouseY: Int) {
        val itemStack = slot.itemStack.takeUnless { it?.isEmpty == true } ?: return
        graphics.renderItem(itemStack, slot.x, slot.y)
        graphics.renderItemDecorations(McFont.self, itemStack, slot.x, slot.y)

        if (slot.mouseOver(mouseX, mouseY)) {
            RenderUtils.drawSlotHighlightBack(graphics, slot.x, slot.y)
            RenderUtils.drawSlotHighlightFront(graphics, slot.x, slot.y)
            graphics.showTooltip(
                Text.multiline(
                    itemStack.getTooltipLines(
                        Item.TooltipContext.of(McLevel.self),
                        McPlayer.self!!,
                        if (McClient.options.advancedItemTooltips) TooltipFlag.ADVANCED else TooltipFlag.NORMAL,
                    ),
                ),
            )
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (McClient.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val offsetX = this.x + 8
        val offsetY = this.y + 18
        val localX = mouseX - offsetX
        val localY = mouseY - offsetY

        if (localX > 0 && localX < backgroundWidth && localY > 0 && localY < backgroundHeight) {
            val slotX = (localX / 18).toInt()
            val slotY = (localY / 18).toInt()

            val index = slotX + slotY * 9
            if (index < 0 || index >= slots.size) {
                return super.mouseClicked(mouseX, mouseY, button)
            }

            slots[index].onClick(index)
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    companion object {
        private val TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png")
    }

    data class Slot(val x: Int, val y: Int, val itemStack: ItemStack? = null, var onClick: (Int) -> Unit = {}) {
        fun mouseOver(mouseX: Int, mouseY: Int) = mouseX > x && mouseX < x + 18 && mouseY > y && mouseY < y + 18

        companion object {
            fun List<ItemStack>.asSlots() = mapIndexed { index, itemStack ->
                val x = (index % 9) * 18
                val y = (index / 9) * 18
                Slot(x, y, itemStack)
            }

            fun playerInventoryAsSlots(rows: Int): List<Slot> {
                return McPlayer.inventory.mapIndexed { index, itemStack ->
                    val slotX = index % 9
                    val tempY = (index / 9) - 1
                    val slotY = if (tempY < 0) tempY + 4 else tempY
                    Slot(slotX * 18, slotY * 18 + (if (tempY < 0) 4 else 0) + rows * 18 + 13, itemStack)
                }
            }
        }
    }
}
