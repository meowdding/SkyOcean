package me.owdding.skyocean.features.item.search.screen

import me.owdding.skyocean.features.item.sources.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import tech.thatgravyboat.skyblockapi.utils.extentions.createSkull

@Suppress("MaxLineLength")
private const val RIFT_SKULL =
    "ewogICJ0aW1lc3RhbXAiIDogMTY4MTkxMjM5OTYxNCwKICAicHJvZmlsZUlkIiA6ICJkMTJiOTk3ZWI2YTQ0ODQ5ODJmNDE1ZTI1NzFlNmY4NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUd2lybGJlbGwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjI2MTkyNjA5ZDZjNDZhZGU3M2U4MDdmYzQwZGJjM2ExYTFhZmJiNDU2YWUxNjU3ODViMGZlODM0ZGQxY2I1NyIKICAgIH0KICB9Cn0="

enum class SearchCategory(val icon: ItemStack, val source: List<ItemSource> = emptyList()) {
    ALL(Items.COMPASS, ItemSources.itemSearchSources.mapNotNull { it.itemSource }),
    STORAGE(Items.ENDER_CHEST, StorageItemSource),
    SACK(Items.BUNDLE, SacksItemSource),
    ISLAND(Items.CHEST, ChestItemSource),
    MUSEUM(Items.GOLD_BLOCK, MuseumItemSource),
    RIFT(createSkull(RIFT_SKULL), RiftItemSource),
    ;

    constructor(icon: ItemLike, vararg source: ItemSource = arrayOf()) : this(icon.asItem().defaultInstance, source.toList())
    constructor(icon: ItemLike, source: List<ItemSource>) : this(icon.asItem().defaultInstance, source)
    constructor(icon: ItemStack, vararg source: ItemSource = arrayOf()) : this(icon, source.toList())
}
