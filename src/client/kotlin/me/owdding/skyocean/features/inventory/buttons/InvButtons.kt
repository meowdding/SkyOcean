package me.owdding.skyocean.features.inventory.buttons

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.inventory.InventoryConfig
import me.owdding.skyocean.config.features.inventory.Buttons
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.ChatUtils
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import net.fabricmc.fabric.api.client.screen.v1.Screens
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
        if (showButtons(event.screen)) return
        val screen = event.screen as AbstractContainerScreen<*>

        val buttonOffset = 8
        val buttonHeight = 32

        for (y in 0 until 2) {
            for (x in 0 until 7) {
                val posX = (x * (screen.containerWidth / 7)) + screen.left
                val yOffset = if (y == 0) {
                    -buttonHeight + buttonOffset
                } else {
                    -buttonOffset - 1
                }
                val posY = (y * screen.containerHeight) + screen.top + yOffset

                val button = Buttons.buttons[x + y * 7]
                event.widgets.add(
                    InvButton(button, x, y == 1, screen, x + y * 7, posX, posY, 26, buttonHeight)
                        .withSize(26, buttonHeight)
                        .withCallback {
                            if (screen is ButtonConfigScreen) {
                                screen.refresh(x + y * 7)
                            } else {
                                val command = button.command.replace("/", "")
                                McClient.connection?.sendCommand(command)
                            }
                        }
                        .withTooltip(Text.of(button.tooltip.takeIf { it.isNotEmpty() } ?: button.command))
                        .withPosition(posX, posY)
                )
            }
        }
    }

    @Subscription
    fun onScreenBackground(event: RenderScreenBackgroundEvent) {
        if (showButtons(event.screen)) return
        Screens.getButtons(event.screen).forEach {
            if (it is InvButton && !it.highlight) {
                it.renderButtons(event.graphics, 0, 0, 0F)
            }
        }
    }

    @Subscription
    fun onScreenForeground(event: RenderScreenForegroundEvent) {
        if (showButtons(event.screen)) return
        Screens.getButtons(event.screen).forEach {
            if (it is InvButton && it.highlight) {
                it.renderButtons(event.graphics, 0, 0, 0F)
            }
        }
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("buttons") {
            if (InventoryConfig.inventoryButtons) {
                McClient.setScreenAsync(ButtonConfigScreen(null))
            } else {
                Text.of("First Enable Inventory Buttons in the Config").withColor(0xf38ba8).sendWithPrefix()
            }
        }
    }

    private fun showButtons(screen: Screen): Boolean {
        return screen !is AbstractContainerScreen<*> ||
            !InventoryConfig.inventoryButtons ||
            (!LocationAPI.isOnSkyBlock && screen !is ButtonConfigScreen)
    }

}
