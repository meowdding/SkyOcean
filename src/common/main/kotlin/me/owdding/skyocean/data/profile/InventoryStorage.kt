package me.owdding.skyocean.data.profile

import com.mojang.serialization.Codec
import me.owdding.skyocean.features.inventory.InventoryData
import me.owdding.skyocean.features.inventory.InventoryType
import me.owdding.skyocean.utils.storage.ProfileStorage
import net.minecraft.world.inventory.Slot

object InventoryStorage {
    private val storage: ProfileStorage<InventoryData> = ProfileStorage<InventoryData>(
        0,
        { mutableMapOf() },
        "inventory",
    ) { version ->
        when (version) {
            0 -> InventoryType.CODEC
            else -> Codec.unit<InventoryData> { mutableMapOf() }
        }
    }

    fun setInventory(type: InventoryType, list: MutableList<Slot>) {
        storage.get()?.set(type, list.map { it.index to it.item }.toMutableList())
    }

    fun save() {
        storage.save()
    }

    val data get() = storage.get()
}
