package me.owdding.skyocean.data.profile

import com.mojang.serialization.Codec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.features.inventory.InventoryData
import me.owdding.skyocean.features.inventory.InventoryType
import me.owdding.skyocean.utils.storage.ProfileStorage
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.PlayerInventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

@Module
object InventoryStorage {
    private val storage: ProfileStorage<InventoryData> = ProfileStorage<InventoryData>(
        0,
        { mutableMapOf() },
        "inventory",
    ) { version ->
        when (version) {
            0 -> InventoryType.CODEC
            else -> Codec.unit { mutableMapOf() }
        }
    }

    @Subscription(PlayerInventoryChangeEvent::class)
    @OnlyOnSkyBlock
    fun onInventoryChange() {
        val inventoryType = InventoryType.NORMAL.takeUnless { SkyBlockIsland.THE_RIFT.inIsland() } ?: InventoryType.RIFT

        setInventory(inventoryType, McPlayer.inventory)
    }

    fun setInventory(type: InventoryType, list: List<ItemStack>) {
        data?.set(type, list.toMutableList())
    }

    fun save() {
        storage.save()
    }

    val data get() = storage.get()
}
