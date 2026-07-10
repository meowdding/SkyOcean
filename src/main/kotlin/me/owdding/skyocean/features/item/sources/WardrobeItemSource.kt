package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.items.loadout.ArmorWardrobeAPI
import tech.thatgravyboat.skyblockapi.api.profile.items.loadout.EquipmentWardrobeAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object ArmorWardrobeItemSource : ItemSource {
    override fun getAll() = ArmorWardrobeAPI.slots
        .filter { (slot, _) -> slot != ArmorWardrobeAPI.currentSlot }
        .flatMap { (slot, items) -> items.map { SimpleTrackedItem(it, ArmorWardrobeContext(slot)) } }

    override val type = ItemSources.WARDROBE
}
data class ArmorWardrobeContext(
    val slot: Int,
) : ItemContext {
    override val source = ItemSources.WARDROBE
    override fun collectLines() = build {
        add("Armor Wardrobe Slot $slot") { color = TextColor.GRAY }
        requiresOverworld { add("Click to open armor wardrobe!") { this.color = TextColor.YELLOW } }
        riftWarning()
    }

    override fun open() = requiresOverworld(true) { McClient.sendCommand("wd ${(slot - 1) / 9 + 1}") }
}

object EquipmentWardrobeItemSource : ItemSource {

    override fun getAll() = EquipmentWardrobeAPI.slots
        .filter { (slot, _) -> slot != EquipmentWardrobeAPI.currentSlot }
        .flatMap { (slot, items) -> items.map { SimpleTrackedItem(it, EquipmentWardrobeContext(slot)) } }

    override val type = ItemSources.EQUIPMENT_WARDROBE
}

data class EquipmentWardrobeContext(
    val slot: Int,
) : ItemContext {
    override val source = ItemSources.EQUIPMENT_WARDROBE
    override fun collectLines() = build {
        add("Equipment Wardrobe Slot $slot") { color = TextColor.GRAY }
        requiresOverworld { add("Click to open equipment wardrobe!") { this.color = TextColor.YELLOW } }
        riftWarning()
    }

    override fun open() = requiresOverworld(true) { McClient.sendCommand("eq ${(slot - 1) / 9 + 1}") }
}
