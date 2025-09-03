package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

interface ItemSource {

    fun getAll(): List<SimpleTrackedItem>
    fun postProcess(items: List<SimpleTrackedItem>): List<SimpleTrackedItem> = emptyList()
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
    RIFT(RiftItemSource),
    DRILL_UPGRADE(DrillUpgradeItemSource),
    ROD_UPGRADE(RodUpgradesItemSource),
    HUNTAXE(HuntaxeItemSource),
    TOOLKIT(ToolkitItemSource)
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
            val entries = entries.filter { ItemSearchScreen.category.source.isEmpty() || it.itemSource in ItemSearchScreen.category.source }

            val list = entries.mapNotNull { it.itemSource?.getAll() }.flatten().filterNot { (itemStack, _) -> itemStack.isEmpty }

            return buildList {
                addAll(list)
                addAll(entries.mapNotNull { it.itemSource?.postProcess(list) }.flatten())
            }
        }
    }
}
