package me.owdding.skyocean.features.misc.itemsearch.item

import me.owdding.skyocean.features.misc.itemsearch.ItemContext
import net.minecraft.world.item.ItemStack

data class SimpleTrackedItem(
    override val itemStack: ItemStack,
    override val context: ItemContext,
) : TrackedItem {
    override fun add(other: TrackedItem) = TrackedItemBundle(this).apply { this.add(other) }
}
