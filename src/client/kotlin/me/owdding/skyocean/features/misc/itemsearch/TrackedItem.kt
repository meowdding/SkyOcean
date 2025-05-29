package me.owdding.skyocean.features.misc.itemsearch

import net.minecraft.world.item.ItemStack

data class TrackedItem(
    val itemStack: ItemStack,
    val context: ItemContext,
)
