package me.owdding.skyocean.features.misc.itemsearch.search.query.operators

import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

interface πQueryElement : QueryOperator, Predicate<ItemStack> {
}
