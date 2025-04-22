package codes.cookies.skyocean.utils.tags

import codes.cookies.skyocean.SkyOcean
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTagKey

enum class ItemTagKey(path: String) : ItemTagKey {
    HOTM_PERK_ITEMS("hotm_perk_items"),
    ;

    override val key: TagKey<Item> = TagKey.create(Registries.ITEM, SkyOcean.id(path))
}
