package me.owdding.skyocean.features.inventory

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.utils.codecs.CodecHelpers
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack

@GenerateCodec
data class DimensionInventory(
    val inventory: MutableList<ItemStack> = mutableListOf(),
    val armour: MutableMap<EquipmentSlot, ItemStack> = mutableMapOf(),
)

typealias InventoryData = MutableMap<InventoryType, DimensionInventory>

enum class InventoryType {
    NORMAL,
    RIFT,
    ;

    companion object {
        val CODEC: Codec<InventoryData> = CodecHelpers.mutableMap()
    }
}
