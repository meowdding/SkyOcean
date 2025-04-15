package codes.cookies.skyocean.helpers

import codes.cookies.skyocean.helpers.ClientSideInventory.Slot.Companion.asSlots
import codes.cookies.skyocean.utils.ChatUtils
import codes.cookies.skyocean.utils.RenderUtils
import codes.cookies.skyocean.utils.RenderUtils.translated
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

abstract class ClientSideInventory(val titleComponent: Component?, val rows: Int) : Screen(titleComponent) {
    val backgroundHeight = 114 + rows * 18;
    val backgroundWidth = 176
    val x get() = (this.width - backgroundWidth) / 2
    val y get() = (this.height - backgroundHeight) / 2
    var slots: MutableList<Slot> = mutableListOf()
    val renderBackground = true

    fun addItems(items: List<ItemStack>) {
        slots = items.asSlots().toMutableList()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (titleComponent != null) {
            guiGraphics.drawString(McFont.self, titleComponent, this.x + 8, this.y + 6, -1, false);
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
        val i = (this.width - backgroundWidth) / 2
        val j = (this.height - this.backgroundHeight) / 2
        guiGraphics.blit(
            RenderType::guiTextured,
            TEXTURE,
            i,
            j,
            0.0F,
            0.0F,
            backgroundWidth,
            this.rows * 18 + 17,
            256,
            256
        )
        guiGraphics.blit(
            RenderType::guiTextured,
            TEXTURE,
            i,
            j + this.rows * 18 + 17,
            0.0F,
            126.0F,
            backgroundWidth,
            96,
            256,
            256
        )
    }

    private fun renderSlot(graphics: GuiGraphics, slot: Slot, mouseX: Int, mouseY: Int) {
        val itemStack = slot.itemStack.takeUnless { it?.isEmpty == true } ?: return
        graphics.renderItem(itemStack, slot.x, slot.y)
        graphics.renderItemDecorations(McFont.self, itemStack, slot.x, slot.y)

        if (slot.mouseOver(mouseX, mouseY)) {
            RenderUtils.drawSlotHighlightBack(graphics, slot.x, slot.y)
            RenderUtils.drawSlotHighlightFront(graphics, slot.x, slot.y)
            graphics.renderTooltip(McFont.self, itemStack, mouseX, mouseY)
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

    data class Slot(val x: Int, val y: Int, val itemStack: ItemStack? = null, val onClick: (Int) -> Unit = {}) {
        fun mouseOver(mouseX: Int, mouseY: Int) = mouseX > x && mouseX < x + 18 && mouseY > y && mouseY < y + 18

        companion object {
            fun List<ItemStack>.asSlots() = mapIndexed { index, itemStack ->
                val x = (index % 9) * 18
                val y = (index / 9) * 18
                Slot(x, y, itemStack) {
                    ChatUtils.chat("Clicked on slot $index")
                }
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
