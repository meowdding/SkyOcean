package me.owdding.skyocean.features.item.custom

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.SkyOceanKeybind
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot

@Module
object CustomizeKeybind {
    private val keybind = SkyOceanKeybind("item_customize")

    @Subscription
    @OnlyOnSkyBlock
    fun onKeybind(event: ScreenKeyReleasedEvent.Pre) {
        if (!keybind.matches(event)) return

        val hovered = McScreen.asMenu?.getHoveredSlot()?.item?.takeUnless { it.isEmpty } ?: return

        CustomItemsHelper.tryAndOpenCustomizationUi(hovered)
    }
}
