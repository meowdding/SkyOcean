package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object InventoryItemSource : ItemSource {
    override fun getAll() = buildList {
        addAll(McPlayer.inventory.map { SimpleTrackedItem(it, InventoryItemContext) })
        fun addEquipment(stack: ItemStack) {
            add(SimpleTrackedItem(stack, EquipmentItemContext))
        }

        addEquipment(McPlayer.helmet)
        addEquipment(McPlayer.chestplate)
        addEquipment(McPlayer.leggings)
        addEquipment(McPlayer.boots)
    }

    override fun remove(item: SimpleTrackedItem) {
        TODO("Not yet implemented")
    }

    override val type = ItemSources.INVENTORY
}

interface OnPlayerItemContext : ItemContext {
    override val source get() = ItemSources.INVENTORY
}

object EquipmentItemContext : OnPlayerItemContext {
    override fun collectLines() = build { add("You are wearing this item!") { color = TextColor.GRAY } }
}

object InventoryItemContext : OnPlayerItemContext {
    override fun collectLines() = build { add("This item is in your inventory!") { color = TextColor.GRAY } }
}
