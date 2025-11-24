package me.owdding.skyocean.data.profile

import me.owdding.ktmodules.Module
import me.owdding.skyocean.features.inventory.DimensionInventory
import me.owdding.skyocean.features.inventory.InventoryData
import me.owdding.skyocean.features.inventory.InventoryType
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.storage.ProfileStorage
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.PlayerInventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

@Module
object InventoryStorage {
    private val storage: ProfileStorage<InventoryData> = ProfileStorage(
        defaultData = { mutableMapOf() },
        fileName = "inventory",
        version = 1,
        codec = {
            when (it) {
                0 -> CodecUtils.map(
                    SkyOceanCodecs.getCodec<InventoryType>(),
                    CodecUtils.mutableList(ItemStack.OPTIONAL_CODEC),
                ).xmap(
                    { it.mapValues { (_, value) -> DimensionInventory(value) }.toMutableMap() },
                    { it.mapValues { (_, value) -> value.inventory }.toMutableMap() },
                )

                else -> InventoryType.CODEC
            }
        },
    )

    val data get() = storage.get()

    @Subscription(PlayerInventoryChangeEvent::class)
    @OnlyOnSkyBlock
    fun onInventoryChange() {
        val inventoryType = InventoryType.NORMAL.takeUnless { SkyBlockIsland.THE_RIFT.inIsland() } ?: InventoryType.RIFT

        setInventory(inventoryType, McPlayer.inventory)
        setArmour(inventoryType, EquipmentSlot.HEAD, McPlayer.helmet)
        setArmour(inventoryType, EquipmentSlot.CHEST, McPlayer.chestplate)
        setArmour(inventoryType, EquipmentSlot.LEGS, McPlayer.leggings)
        setArmour(inventoryType, EquipmentSlot.FEET, McPlayer.boots)
    }

    fun setInventory(type: InventoryType, list: List<ItemStack>) {
        data?.getOrPut(type, ::DimensionInventory)?.inventory?.apply {
            this.clear()
            this.addAll(list)
        }
        save()
    }

    fun setArmour(type: InventoryType, equipmentSlot: EquipmentSlot, item: ItemStack) {
        data?.getOrPut(type, ::DimensionInventory)?.armour[equipmentSlot] = item
        save()
    }

    fun save() {
        storage.save()
    }
}
