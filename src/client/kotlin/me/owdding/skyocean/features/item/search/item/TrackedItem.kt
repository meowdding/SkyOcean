package me.owdding.skyocean.features.item.search.item

import me.owdding.skyocean.features.item.search.ItemContext
import net.minecraft.world.item.ItemStack

interface TrackedItem {

    val itemStack: ItemStack
    val context: ItemContext
    fun add(other: TrackedItem): TrackedItem

    operator fun component1() = itemStack
    operator fun component2() = context

}
