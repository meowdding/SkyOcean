package me.owdding.skyocean.features.item.search.screen

import me.owdding.skyocean.features.item.sources.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike

enum class SearchCategory(val icon: ItemStack, val source: ItemSource? = null) {
    ALL(Items.COMPASS),
    STORAGE(Items.ENDER_CHEST, StorageItemSource),
    SACK(Items.BUNDLE, SacksItemSource),
    ISLAND(Items.CHEST, ChestItemSource),
    MUSEUM(Items.GOLD_BLOCK, MuseumItemSource),
    ;

    constructor(icon: ItemLike, source: ItemSource? = null) : this(icon.asItem().defaultInstance, source)
}
