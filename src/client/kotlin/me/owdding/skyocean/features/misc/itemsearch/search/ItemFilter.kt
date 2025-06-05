package me.owdding.skyocean.features.misc.itemsearch.search

import me.owdding.skyocean.features.misc.itemsearch.item.TrackedItem
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

interface ItemFilter : Predicate<ItemStack> {

    fun test(item: TrackedItem): Boolean = this.test(item.itemStack)

}
