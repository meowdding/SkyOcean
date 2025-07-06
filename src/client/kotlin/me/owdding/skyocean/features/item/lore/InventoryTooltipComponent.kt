package me.owdding.skyocean.features.item.lore

import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.toColumn
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withPadding
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.world.item.ItemStack
import kotlin.math.nextUp

class InventoryTooltipComponent(
    val items: List<ItemStack>,
    columns: Int,
) : ClientTooltipComponent {
    val rows = (items.size / columns.toFloat()).nextUp().toInt()

    val width = columns * 20 + 4 + 10
    val height = rows * 20 + 4 + 10

    val display = ExtraDisplays.inventoryBackground(
        columns,
        rows,
        items.map { Displays.item(it, showStackSize = true).withPadding(2) }.chunked(columns).map { it.toRow() }.toColumn().withPadding(2),
    )

    override fun getHeight(font: Font) = height
    override fun getWidth(font: Font) = width

    override fun renderImage(font: Font, x: Int, y: Int, width: Int, height: Int, guiGraphics: GuiGraphics) {
        display.render(guiGraphics, x + 5, y + 5)
    }
}
