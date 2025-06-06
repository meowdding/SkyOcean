package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.wardrobe.WardrobeAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient

object WardrobeItemSource : ItemSource {
    override fun getAll() = WardrobeAPI.slots.flatMap { (slot, items) -> items.map { SimpleTrackedItem(it, WardrobeContext(slot)) } }

    override fun remove(item: SimpleTrackedItem) {
        TODO("Not yet implemented")
    }

    override val type = ItemSources.WARDROBE
}

data class WardrobeContext(
    val slot: Int,
) : ItemContext {
    override val source = ItemSources.WARDROBE
    override fun collectLines() = build {
        add("Wardrobe: $slot")
    }

    override fun open() = McClient.sendCommand("wd ${(slot - 1) / 9 + 1}")
}
