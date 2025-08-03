package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen
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
    MUSEUM(MuseumItemSource),
    RIFT(RiftItemSource)
    ;
    // todo SACK_OF_SACKS(TODO()),
    // todo POTION_BAG(TODO()),

    init {
        if (itemSource == null && ordinal != 0) {
            error("Only BUNDLE might not have a source!")
        }
    }

    companion object {
        fun getAllItems(): Iterable<SimpleTrackedItem> {
            val entries = entries.filter { ItemSearchScreen.category.source == null || it.itemSource == ItemSearchScreen.category.source }

            return entries.mapNotNull { it.itemSource?.getAll() }.flatten().filterNot { (itemStack, _) -> itemStack.isEmpty }
        }
    }
}
