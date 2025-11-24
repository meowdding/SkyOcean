package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.data.profile.InventoryStorage
import me.owdding.skyocean.features.inventory.InventoryType
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.profile.items.equipment.EquipmentAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.getArmor
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object InventoryItemSource : ItemSource {
    override fun getAll() = buildList {
        fun addEquipment(stack: ItemStack) {
            add(SimpleTrackedItem(stack, EquipmentItemContext))
        }

        EquipmentAPI.normalEquipment.values.forEach(::addEquipment)

        if (SkyBlockIsland.THE_RIFT.inIsland()) {
            val overworldData = InventoryStorage.data?.get(InventoryType.NORMAL)
            overworldData?.inventory?.map { SimpleTrackedItem(it, InventoryItemContext) }?.let { addAll(it) }
            overworldData?.armour?.values?.map { SimpleTrackedItem(it, EquipmentItemContext) }?.let { addAll(it) }
        } else {
            addAll(McPlayer.inventory.map { SimpleTrackedItem(it, InventoryItemContext) })
            McPlayer.self?.getArmor()?.forEach(::addEquipment)
        }
    }

    override val type = ItemSources.INVENTORY
}

interface OnPlayerItemContext : ItemContext {
    override val source get() = ItemSources.INVENTORY
}

object EquipmentItemContext : OnPlayerItemContext {
    override fun collectLines() = build {
        requiresOverworld {
            add("Equipped!") { color = TextColor.GRAY }
            add("Click to open equipment menu!") { color = TextColor.GRAY }
        }
        requiresRift {
            add("Equipped in overworld!") { color = TextColor.GRAY }
            add("Not currently in the overworld!") { color = TextColor.RED }
        }
    }

    override fun open() = requiresOverworld(true) { McClient.sendCommand("/eq") }
}

object InventoryItemContext : OnPlayerItemContext {
    override fun collectLines() = build {
        requiresOverworld { add("In your inventory!") { color = TextColor.GRAY } }
        requiresRift { add("In your overworld inventory!") { color = TextColor.GRAY } }
    }
}
