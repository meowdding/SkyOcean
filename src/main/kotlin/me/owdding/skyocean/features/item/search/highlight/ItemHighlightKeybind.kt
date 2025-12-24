package me.owdding.skyocean.features.item.search.highlight

import me.owdding.ktmodules.Module
import me.owdding.lib.compat.REIRuntimeCompatability
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.data.profile.IslandChestStorage
import me.owdding.skyocean.features.item.search.search.ReferenceItemFilter
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot

@Module
object ItemHighlightKeybind {

    private val keybind = SkyOceanKeybind("item_highlight")

    @Subscription
    @OnlyOnSkyBlock
    fun onKeybind(event: ScreenKeyReleasedEvent.Pre) {
        if (!keybind.matches(event)) return

        val reiHovered = REIRuntimeCompatability.getReiHoveredItemStack()
        val mcScreenHovered = McScreen.asMenu?.getHoveredSlot()?.item?.takeUnless { it.isEmpty }
        val item = mcScreenHovered ?: reiHovered ?: return
        val filter = ReferenceItemFilter(item)

        ItemHighlighter.setHighlight(filter)

        // It's run on the next tick because setHighlight also uses runNextTick and clears all chests
        McClient.runNextTick {
            IslandChestStorage.getItems().filter { filter.test(it.itemStack) }.flatMap { (_, _, pos1, pos2) ->
                listOfNotNull(pos1, pos2)
            }.distinct().let(ItemHighlighter::addChests)
            ItemHighlighter.recalculate()
        }
    }

}
