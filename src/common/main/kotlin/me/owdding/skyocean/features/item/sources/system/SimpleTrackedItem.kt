package me.owdding.skyocean.features.item.sources.system

import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue

data class SimpleTrackedItem(
    override val itemStack: ItemStack,
    override val context: ItemContext,
) : TrackedItem {
    override val price get() = itemStack.getItemValue().price

    override fun add(other: TrackedItem) = TrackedItemBundle(this).apply { this.add(other) }
}
