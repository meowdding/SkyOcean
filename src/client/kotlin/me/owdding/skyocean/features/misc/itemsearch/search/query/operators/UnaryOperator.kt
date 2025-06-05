package me.owdding.skyocean.features.misc.itemsearch.search.query.operators

import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

interface UnaryOperator : QueryOperator {

    fun apply(operator: Predicate<ItemStack>): Predicate<ItemStack>

}
