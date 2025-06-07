package me.owdding.skyocean.utils.rendering

import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.displays.Display
import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.utils.extentions.translated

object ExtraDisplays {

    fun inventorySlot(display: Display, color: Int = -1) = inventoryBackground(1, Orientation.HORIZONTAL, display, color)

    fun inventoryBackground(size: Int, orientation: Orientation, display: Display, color: Int = -1): Display {
        return object : Display {
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()

            override fun render(graphics: GuiGraphics) {
                InventoryRenderer.renderMonoInventory(graphics, 0, 0, display.getWidth(), display.getHeight(), size, orientation, color)
                graphics.translated(0, 0, 2) {
                    display.render(graphics)
                }
            }
        }
    }

    fun inventoryBackground(columns: Int, rows: Int, display: Display, color: Int = -1): Display {
        if (rows == 1) {
            return inventoryBackground(columns, Orientation.HORIZONTAL, display, color)
        }
        if (columns == 1) {
            return inventoryBackground(rows, Orientation.VERTICAL, display, color)
        }

        return object : Display {
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()

            override fun render(graphics: GuiGraphics) {
                InventoryRenderer.renderNormalInventory(graphics, 0, 0, display.getWidth(), display.getHeight(), columns, rows, color)
                graphics.translated(0, 0, 2) {
                    display.render(graphics)
                }
            }
        }
    }

}
