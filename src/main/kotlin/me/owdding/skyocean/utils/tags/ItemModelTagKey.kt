package me.owdding.skyocean.utils.tags

import me.owdding.skyocean.SkyOcean
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemModelTagKey

enum class ItemModelTagKey(val path: String) : ItemModelTagKey {
    HOTF_PERK_ITEMS("hotf_perk_items"),
    ;

    override val key: TagKey<Item> = TagKey.create(Registries.ITEM, SkyOcean.id(path))
}
