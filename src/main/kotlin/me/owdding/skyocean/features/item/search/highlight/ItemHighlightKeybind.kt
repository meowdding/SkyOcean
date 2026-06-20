package me.owdding.skyocean.features.item.search.highlight

import me.owdding.ktmodules.Module
import me.owdding.lib.events.ItemListEvent
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.features.item.search.search.ReferenceItemFilter
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot

@Module
object ItemHighlightKeybind {

    private val keybind = SkyOceanKeybind("item_highlight")

    @Subscription
    @OnlyOnSkyBlock
    fun onItemListKeybind(event: ScreenKeyReleasedEvent.Pre) {
        if (!keybind.matches(event)) return
        highlight(McScreen.asMenu?.getHoveredSlot()?.item)
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onItemListKeybind(event: ItemListEvent.HoveredItemKeyPress) {
        if (!keybind.key.matches(event.event)) return
        highlight(event.stack)
    }

    private fun highlight(stack: ItemStack?) {
        val filter = ReferenceItemFilter(stack?.takeUnless { it.isEmpty } ?: return)
        ItemHighlighter.setHighlight(filter)
    }

}
