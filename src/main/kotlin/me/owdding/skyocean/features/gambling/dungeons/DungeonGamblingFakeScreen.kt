package me.owdding.skyocean.features.gambling.dungeons

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.lib.platform.screens.KeyEvent
import me.owdding.skyocean.features.gambling.dungeons.chest.DungeonChestType
import me.owdding.skyocean.utils.SkyOceanScreen
import net.minecraft.client.gui.GuiGraphicsExtractor
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

    //~ if >= 26.1 'render' -> 'extractRenderState'
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        DungeonGamblingRenderer.extract(graphics)
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        return when (keyEvent.key) {
            InputConstants.KEY_R -> {
                setup()
                true
            }
            else -> super.keyPressed(keyEvent)
        }
    }
}
