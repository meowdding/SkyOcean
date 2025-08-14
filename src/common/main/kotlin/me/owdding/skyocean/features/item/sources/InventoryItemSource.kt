package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.data.profile.InventoryStorage
import me.owdding.skyocean.features.inventory.InventoryType
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object InventoryItemSource : ItemSource {
    override fun getAll() = buildList {
        if (SkyBlockIsland.THE_RIFT.inIsland()) {
            InventoryStorage.data?.get(InventoryType.NORMAL)?.map { SimpleTrackedItem(it, InventoryItemContext) }?.toMutableList()?.let { addAll(it) }
        } else {
            addAll(McPlayer.inventory.map { SimpleTrackedItem(it, InventoryItemContext) })
        }
        fun addEquipment(stack: ItemStack) {
            add(SimpleTrackedItem(stack, EquipmentItemContext))
        }

        addEquipment(McPlayer.helmet)
        addEquipment(McPlayer.chestplate)
        addEquipment(McPlayer.leggings)
        addEquipment(McPlayer.boots)
    }

    override val type = ItemSources.INVENTORY
}

interface OnPlayerItemContext : ItemContext {
    override val source get() = ItemSources.INVENTORY
}

object EquipmentItemContext : OnPlayerItemContext {
    override fun collectLines() = build {
        requiresOverworld { add("Equipped!") { color = TextColor.GRAY } }
        requiresRift { add("Equipped in overworld!") { color = TextColor.GRAY } }
    }
}

object InventoryItemContext : OnPlayerItemContext {
    override fun collectLines() = build {
        requiresOverworld { add("In your inventory!") { color = TextColor.GRAY } }
        requiresRift { add("In your overworld inventory!") { color = TextColor.GRAY } }
    }
}
