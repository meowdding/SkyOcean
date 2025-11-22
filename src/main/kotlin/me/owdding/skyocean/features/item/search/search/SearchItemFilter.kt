package me.owdding.skyocean.features.item.search.search

import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore

data class SearchItemFilter(val search: String) : ItemFilter {
    override fun test(item: ItemStack): Boolean {
        return item.cleanName.contains(search, ignoreCase = true) && item.getRawLore().any { it.contains(search, ignoreCase = true) }
    }
}
