package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.items.wardrobe.WardrobeAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object WardrobeItemSource : ItemSource {
    override fun getAll() = WardrobeAPI.slots
        .filter { (slot, _) -> slot != WardrobeAPI.currentSlot }
        .flatMap { (slot, items) -> items.map { SimpleTrackedItem(it, WardrobeContext(slot)) } }

    override val type = ItemSources.WARDROBE
}

data class WardrobeContext(
    val slot: Int,
) : ItemContext {
    override val source = ItemSources.WARDROBE
    override fun collectLines() = build {
        add("Wardrobe Slot $slot") { color = TextColor.GRAY }
        requiresOverworld { add("Click to open wardrobe!") { this.color = TextColor.YELLOW } }
        riftWarning()
    }

    override fun open() = requiresOverworld(true) { McClient.sendCommand("wd ${(slot - 1) / 9 + 1}") }
}
