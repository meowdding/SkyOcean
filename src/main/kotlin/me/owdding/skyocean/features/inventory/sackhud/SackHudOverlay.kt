package me.owdding.skyocean.features.inventory.sackhud

import earth.terrarium.olympus.client.ui.context.ContextMenu
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.overlays.Position
import me.owdding.skyocean.config.CachedValue
import me.owdding.skyocean.config.features.inventory.SackHudConfig
import me.owdding.skyocean.config.hidden.OverlayPositions
import me.owdding.skyocean.data.profile.SackHudStorage
import me.owdding.skyocean.repo.SackData
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.rendering.OceanTextures
import me.owdding.skyocean.utils.rendering.Overlay
import me.owdding.skyocean.utils.rendering.SkyOceanOverlay
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.translated
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import kotlin.time.Duration.Companion.seconds

@Overlay
object SackHudOverlay : SkyOceanOverlay() {

    private val displayCache = CachedValue(1.seconds) { buildDisplay() }
    private val display: Display by displayCache

    private val background get() = SackHudConfig.background
    private val padding get() = if (background) 3 else 0

    override val name: Component = +"overlays.sack_hud"
    override val position: Position = OverlayPositions.sackHud
    override val bounds: Pair<Int, Int> get() = (display.getWidth() + padding * 2) to (display.getHeight() + padding * 2)
    override val enabled: Boolean get() = SackHudConfig.enabled && LocationAPI.isOnSkyBlock && SackHudStorage.items.isNotEmpty()

    private fun buildDisplay(): Display {
        if (SackHudStorage.items.isEmpty()) return Displays.empty(0, 0)

        return DisplayFactory.vertical {
            SackHudStorage.items.forEach { id ->
                val stack = SackData.sackItems[id]?.create() ?: return@forEach
                horizontal(5, Alignment.CENTER) {
                    display(Displays.item(stack))
                    string(stack.hoverName)
                    val amount = SackData.amountOf(id)
                    string(Text.of("x${amount.toFormattedString()}", TextColor.PINK))
                }
            }
        }
    }

    override fun extract(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (background) {
            graphics.drawSprite(OceanTextures.overlayBackground, 0, 0, bounds.first, bounds.second)
        }

        graphics.translated(padding, padding) {
            display.extract(graphics)
        }
    }

    override fun onRightClick() = ContextMenu.open {
        it.button(+"overlays.sack_hud.edit") {
            McClient.setScreenAsync { SackHudEditScreen() }
        }
        it.button(+"overlays.background.${if (background) "disable" else "enable"}") {
            SackHudConfig.background = !background
        }
        it.divider()
        it.dangerButton(Text.translatable("mlib.overlay.edit.reset")) {
            position.resetPosition()
        }
    }
}
