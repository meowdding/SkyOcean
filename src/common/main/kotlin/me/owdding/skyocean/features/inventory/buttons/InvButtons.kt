package me.owdding.skyocean.features.inventory.buttons

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.inventory.Buttons
import me.owdding.skyocean.config.features.inventory.InventoryConfig
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.render.RenderScreenBackgroundEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderScreenForegroundEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.containerHeight
import tech.thatgravyboat.skyblockapi.utils.extentions.containerWidth
import tech.thatgravyboat.skyblockapi.utils.extentions.left
import tech.thatgravyboat.skyblockapi.utils.extentions.top
import tech.thatgravyboat.skyblockapi.utils.text.Text

@Module
object InvButtons {
    @Subscription
    fun onScreen(event: ScreenInitializedEvent) {
        if (!shouldShowButtons(event.screen)) return
        val screen = event.screen as AbstractContainerScreen<*>

        val buttonOffset = 8
        val buttonHeight = 32

        for (y in 0 until 2) {
            for (x in 0 until 7) {
                val posX = (x * (screen.containerWidth / 7)) + screen.left
                val yOffset = if (y == 0) {
                    -buttonHeight + buttonOffset
                } else {
                    -buttonOffset - if (screen is ButtonConfigScreen) 0 else 1
                }
                val posY = (y * screen.containerHeight) + screen.top + yOffset

                val button = Buttons.buttons[x + y * 7]
                if (button.disabled && screen !is ButtonConfigScreen) continue
                event.widgets.add(
                    InvButton(button, x, y == 1, screen, x + y * 7, posX, posY, 26, buttonHeight)
                        .withSize(26, buttonHeight)
                        .withCallback {
                            if (screen is ButtonConfigScreen) {
                                screen.refresh(x + y * 7)
                            } else {
                                McClient.sendClientCommand(button.command)
                            }
                        }
                        .withTooltip(Text.of(button.tooltip.takeIf { it.isNotEmpty() } ?: button.command))
                        .withPosition(posX, posY),
                )
            }
        }
    }

    fun onScreenBackgroundAfter(screen: AbstractContainerScreen<*>, graphics: GuiGraphics) {
        if (!shouldShowButtons(screen)) return
        Screens.getButtons(screen).forEach {
            if (it is InvButton && !it.highlight) {
                it.renderItem(graphics)
            }
        }
    }

    @Subscription
    fun onScreenBackground(event: RenderScreenBackgroundEvent) {
        if (!shouldShowButtons(event.screen)) return
        Screens.getButtons(event.screen).forEach {
            if (it is InvButton && !it.highlight) {
                it.renderButtons(event.graphics, 0, 0, 0F)
            }
        }
    }

    @Subscription
    fun onScreenForeground(event: RenderScreenForegroundEvent) {
        if (!shouldShowButtons(event.screen)) return
        Screens.getButtons(event.screen).forEach {
            if (it is InvButton && it.highlight) {
                it.renderButtons(event.graphics, 0, 0, 0F)
                it.renderItem(event.graphics)
            }
        }
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("buttons") {
            if (InventoryConfig.inventoryButtons) {
                McClient.setScreenAsync { ButtonConfigScreen(null) }
            } else {
                (+"skyocean.inventory.buttons.enable_first").withColor(OceanColors.WARNING).sendWithPrefix()
            }
        }
    }

    private fun shouldShowButtons(screen: Screen): Boolean {
        return screen is AbstractContainerScreen<*> && InventoryConfig.inventoryButtons && (LocationAPI.isOnSkyBlock || screen is ButtonConfigScreen)
    }

}
