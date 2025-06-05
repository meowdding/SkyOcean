package me.owdding.skyocean.features.misc.itemsearch.search.query.elements

import com.mojang.brigadier.StringReader
import me.owdding.skyocean.features.misc.itemsearch.search.query.operators.QueryOperator
import me.owdding.skyocean.features.misc.itemsearch.search.query.operators.UnaryOperator
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

object NotOperator : UnaryOperator {
    override fun apply(operator: Predicate<ItemStack>): Predicate<ItemStack> = operator.negate()

    fun tryParse(stringReader: StringReader): QueryOperator? = NotOperator.takeIf { stringReader.peek() == '!' }
}
