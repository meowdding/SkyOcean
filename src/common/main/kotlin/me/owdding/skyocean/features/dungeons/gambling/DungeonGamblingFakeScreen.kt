package me.owdding.skyocean.features.dungeons.gambling

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.skyocean.features.dungeons.gambling.chest.DungeonChestType
import me.owdding.skyocean.utils.SkyOceanScreen
import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor

class DungeonGamblingFakeScreen(
    private val floor: DungeonFloor,
    private val chest: DungeonChestType,
) : SkyOceanScreen() {

    init {
        setup()
    }

    private fun setup() {
        DungeonGamblingRenderer.init(floor, chest)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.render(graphics, mouseX, mouseY, partialTicks)

        DungeonGamblingRenderer.render(graphics)
    }

    override fun keyPressed(keycode: Int, scancode: Int, modifiers: Int): Boolean {
        return when (keycode) {
            InputConstants.KEY_R -> {
                setup()
                true
            }
            else -> super.keyPressed(keycode, scancode, modifiers)
        }
    }
}
