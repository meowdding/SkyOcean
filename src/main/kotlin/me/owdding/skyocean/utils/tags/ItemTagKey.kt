package me.owdding.skyocean.utils.tags

import me.owdding.skyocean.SkyOcean
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTagKey

enum class ItemTagKey(path: String) : ItemTagKey {
    HOTM_PERK_ITEMS("hotm_perk_items"),
    TRIM_PATTERS("trim_patterns"),
    ;

    override val key: TagKey<Item> = TagKey.create(Registries.ITEM, SkyOcean.id(path))
}
