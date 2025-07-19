package me.owdding.skyocean.utils.rendering

import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.client.gui.GuiGraphics
import net.msrandom.stub.Stub

@Stub
expect object InventoryRenderer {

    fun renderMonoInventory(graphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int, size: Int, orientation: Orientation, color: Int)

    fun renderNormalInventory(graphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int, columns: Int, rows: Int, color: Int)
}
