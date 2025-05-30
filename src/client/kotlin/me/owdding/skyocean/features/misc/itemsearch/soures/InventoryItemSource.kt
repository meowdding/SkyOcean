package me.owdding.skyocean.features.misc.itemsearch.soures

import me.owdding.skyocean.features.misc.itemsearch.ItemContext
import me.owdding.skyocean.features.misc.itemsearch.item.SimpleTrackedItem
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

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
    override fun collectLines() = build { add("You are wearing this item!") }
}

object InventoryItemContext : OnPlayerItemContext {
    override fun collectLines() = build { add("This item is in your inventory!") }
}
