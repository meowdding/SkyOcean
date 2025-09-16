package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.minecraft.world.item.ItemStack

interface ItemSource {

    fun getAll(): List<SimpleTrackedItem>
    fun postProcess(items: List<SimpleTrackedItem>): List<SimpleTrackedItem> = emptyList()
    val type: ItemSources

    fun createFromIdAndAmount(id: SkyOceanItemId?, amount: Int): ItemStack? = id?.toItem()?.copyWithCount(amount)

}

enum class ItemSources(val itemSource: ItemSource?, vararg val disabledIn: ItemSourceTag) {
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
    RIFT(RiftItemSource, ItemSourceTag.ITEM_SEARCH),
    DRILL_UPGRADE(DrillUpgradeItemSource),
    ROD_UPGRADE(RodUpgradesItemSource),
    HUNT_AXE(HuntaxeItemSource),
    TOOLKIT(ToolkitItemSource),
    SACK_OF_SACKS(SackOfSacksItemSource),
    HUNTING_BOX(HuntingBoxItemSource, ItemSourceTag.ITEM_SEARCH),
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

        fun getMatching(vararg disabledTags: ItemSourceTag) = entries.filterNot { itemSource -> itemSource.disabledIn.any { disabledTags.contains(it) } }
        val craftHelperSources = getMatching(ItemSourceTag.CRAFT_HELPER)
        val itemSearchSources = getMatching(ItemSourceTag.ITEM_SEARCH)
    }
}

enum class ItemSourceTag {
    ITEM_SEARCH,
    CRAFT_HELPER,
    ;
}
