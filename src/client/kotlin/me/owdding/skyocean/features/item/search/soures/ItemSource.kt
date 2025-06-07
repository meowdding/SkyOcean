package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

interface ItemSource {

    fun getAll(): List<SimpleTrackedItem>
    val type: ItemSources

    fun createFromIdAndAmount(id: String, amount: Int): ItemStack? = RepoItemsAPI.getItemOrNull(id)?.copyWithCount(amount)

}

enum class ItemSources(val itemSource: ItemSource?) {
    BUNDLE(null),
    CHEST(ChestItemSource),
    STORAGE(StorageItemSource),
    WARDROBE(WardrobeItemSource),
    SACKS(SacksItemSource),
    ACCESSORY_BAG(AccessoryBagItemSource),
    FORGE(ForgeItemSource),
    INVENTORY(InventoryItemSource),
    VAULT(VaultItemSource),
    ;
    // todo SACK_OF_SACKS(TODO()),
    // todo POTION_BAG(TODO()),

    init {
        require(itemSource != null && ordinal != 0) { "Only BUNDLE might not have a source!" }
    }

    companion object {
        fun getAllItems(): Iterable<SimpleTrackedItem> =
            entries.mapNotNull { it.itemSource?.getAll() }.flatten().filterNot { (itemStack, _) -> itemStack.isEmpty }
    }
}
