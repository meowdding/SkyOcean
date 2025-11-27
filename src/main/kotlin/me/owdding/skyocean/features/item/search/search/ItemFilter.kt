package me.owdding.skyocean.features.item.search.search

import me.owdding.skyocean.features.item.sources.system.TrackedItem
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

interface ItemFilter : Predicate<ItemStack> {

    fun test(item: TrackedItem): Boolean = this.test(item.itemStack)

}
