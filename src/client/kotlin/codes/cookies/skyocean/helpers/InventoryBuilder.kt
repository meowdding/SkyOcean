package codes.cookies.skyocean.helpers

import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

class InventoryBuilder(val maxSize: Int = 54) {

    private val items: MutableMap<Int, ItemStack> = mutableMapOf()

    fun add(slot: Int, item: ItemStack) {
        if (slot >= maxSize || slot < 0) {
            throw IllegalStateException("Inventory is full")
        }
        items[slot] = item
    }

    fun add(slot: Int, item: ItemLike) = add(slot, item.asItem().defaultInstance)

    fun add(x: Int, y: Int, item: ItemStack) {
        val slot = x + y * 9
        if (slot >= maxSize || slot < 0) {
            throw IllegalStateException("Inventory is full")
        }
        items[slot] = item
    }

    fun add(x: Int, y: Int, item: ItemLike) = add(x, y, item.asItem().defaultInstance)

    fun fill(item: ItemStack) {
        for (i in 0 until maxSize) {
            if (!items.containsKey(i)) {
                items[i] = item
            }
        }
    }

    fun fill(item: ItemLike) = fill(item.asItem().defaultInstance)

    fun build(): List<ItemStack> = items.entries.sortedBy { it.key }.map { it.value }.toList()

}
