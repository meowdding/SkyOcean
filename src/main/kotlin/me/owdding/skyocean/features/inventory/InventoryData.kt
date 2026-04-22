package me.owdding.skyocean.features.inventory

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.extensions.asBlueprint
import me.owdding.skyocean.utils.items.ItemStackBlueprint
import me.owdding.skyocean.utils.levelBound
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack

@GenerateCodec
data class DimensionInventory(
    @FieldName("inventory") val inventoryTemplate: MutableList<ItemStackBlueprint> = mutableListOf(),
    @FieldName("armour") val armourTemplate: MutableMap<EquipmentSlot, ItemStackBlueprint> = mutableMapOf(),
) {
    fun updateInventory(list: List<ItemStack>) {
        inventoryDelegate.invalidate()
        inventoryTemplate.clear()
        inventoryTemplate.addAll(list.map { it.asBlueprint() })
    }

    fun updateArmour(slot: EquipmentSlot, item: ItemStack) {
        armourDelegate.invalidate()
        armourTemplate.clear()
        armourTemplate[slot] = item.asBlueprint()
    }

    private val inventoryDelegate = levelBound { inventoryTemplate.mapTo(ArrayList()) { it.create() } }
    val inventory: List<ItemStack> by inventoryDelegate
    private val armourDelegate = levelBound { armourTemplate.mapValuesTo(LinkedHashMap(armourTemplate.size)) { (_, value) -> value.create() } }
    val armour: Map<EquipmentSlot, ItemStack> by armourDelegate
}

typealias InventoryData = MutableMap<InventoryType, DimensionInventory>

enum class InventoryType {
    NORMAL,
    RIFT,
    ;

    companion object {
        val CODEC: Codec<InventoryData> = CodecHelpers.mutableMap()
    }
}
