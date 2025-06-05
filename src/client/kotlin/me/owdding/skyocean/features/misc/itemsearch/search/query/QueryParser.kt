package me.owdding.skyocean.features.misc.itemsearch.search.query

import com.google.common.base.Predicates
import com.mojang.brigadier.StringReader
import me.owdding.skyocean.features.misc.itemsearch.search.query.elements.AndOperator
import me.owdding.skyocean.features.misc.itemsearch.search.query.elements.NotOperator
import me.owdding.skyocean.features.misc.itemsearch.search.query.elements.OrOperator
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

object QueryParser {

    private val unaryOperators = listOf(
        NotOperator::tryParse,
    )
    private val logicOperators = listOf(
        AndOperator::tryParse,
        OrOperator::tryParse,
    )
    private val queryOperators = listOf(TODO())

    fun parse(query: String): Predicate<ItemStack> {
        val reader = StringReader(query)


        return Predicates.alwaysTrue()
    }
}
