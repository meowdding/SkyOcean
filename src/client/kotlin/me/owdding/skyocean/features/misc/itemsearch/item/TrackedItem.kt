package me.owdding.skyocean.features.misc.itemsearch.item

import me.owdding.skyocean.features.misc.itemsearch.ItemContext
import net.minecraft.world.item.ItemStack

interface TrackedItem {

    val itemStack: ItemStack
    val context: ItemContext
    fun add(other: TrackedItem): TrackedItem

    operator fun component1() = itemStack
    operator fun component2() = context

}
