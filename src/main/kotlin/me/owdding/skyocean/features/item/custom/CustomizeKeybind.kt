package me.owdding.skyocean.features.item.custom

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.features.item.custom.CustomItems.getKey
import me.owdding.skyocean.features.item.custom.data.IdKey
import me.owdding.skyocean.features.item.custom.ui.standard.StandardCustomizationUi
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object CustomizeKeybind {
    private val key = SkyOceanKeybind("customize")

    @Subscription
    fun onKeyPress(event: ScreenKeyReleasedEvent) {
        if (!key.matches(event)) return
        val screen = event.screen as? AbstractContainerScreen<*> ?: return
        val item = screen.getHoveredSlot()?.item ?: return

        if (item.isEmpty) {
            Text.of("You aren't hovering an item!").sendWithPrefix()
            return
        }

        if (item.getKey() == null) {
            Text.of {
                append(item.hoverName)
                append(" can't be customized!")
            }.sendWithPrefix()
            return
        }

        if (item.getKey() is IdKey) {
            Text.of {
                append("Modification will be visible on all variants of this item!")
                this.color = OceanColors.WARNING
            }.sendWithPrefix()
        }

        McClient.runNextTick {
            event.screen.onClose()
            StandardCustomizationUi.open(item)
        }
    }
}
