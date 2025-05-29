package me.owdding.skyocean.features.misc.itemsearch.soures

import me.owdding.skyocean.features.misc.itemsearch.TrackedItem

interface ItemSource {

    fun getAll(): List<TrackedItem>
    fun remove(item: TrackedItem)
    val type: ItemSources

}

enum class ItemSources(val itemSource: ItemSource) {
    CHEST(ChestItemSource),
    STORAGE(StorageItemSource),
    WARDROBE(WardrobeItemSource),
    SACKS(SacksItemSource),
    INVENTORY(TODO()),
    FORGE(TODO()),
    VAULT(TODO()),
    SACK_OF_SACKS(TODO()),
    POTION_BAG(TODO()),
    ACCESSORY_BAG(TODO())
    ;

    companion object {
        fun getAllItems(): Iterable<TrackedItem> = entries.flatMap { it.itemSource.getAll() }.filterNot { (itemStack, _) -> itemStack.isEmpty }
    }
}
