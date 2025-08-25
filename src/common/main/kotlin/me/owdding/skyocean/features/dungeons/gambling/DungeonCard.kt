package me.owdding.skyocean.features.dungeons.gambling

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.platform.drawGradient

class DungeonCard(
    val item: ItemStack,
    val color: Int
) : Renderable {

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        graphics.drawGradient(0, 0, WIDTH, HEIGHT - 1, BACKGROUND_COLOR, ARGB.color(0x60, color), ARGB.color(0x60, color), BACKGROUND_COLOR)
        graphics.drawGradient(0, HEIGHT - 1, WIDTH, 1, color, color, color, color)

        graphics.renderItem(item, WIDTH / 2 - 8, HEIGHT / 2 - 8)
    }

    companion object {

        const val WIDTH = 24
        const val HEIGHT = 18

        const val BACKGROUND_COLOR = 0x80303030.toInt()
    }
}
