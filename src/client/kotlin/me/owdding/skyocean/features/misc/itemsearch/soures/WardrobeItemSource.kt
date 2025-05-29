package me.owdding.skyocean.features.misc.itemsearch.soures

import me.owdding.skyocean.features.misc.itemsearch.ItemContext
import me.owdding.skyocean.features.misc.itemsearch.TrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.wardrobe.WardrobeAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient

object WardrobeItemSource : ItemSource {
    override fun getAll() = WardrobeAPI.slots.flatMap { (slot, items) -> items.map { TrackedItem(it, WardrobeContext(slot)) } }

    override fun remove(item: TrackedItem) {
        TODO("Not yet implemented")
    }

    override val type = ItemSources.WARDROBE
}

data class WardrobeContext(
    val slot: Int,
) : ItemContext {
    override fun collectLines() = build {
        add("Wardrobe: $slot")
    }

    override fun open() = McClient.sendCommand("wd")
}
