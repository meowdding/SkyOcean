package me.owdding.skyocean.features.misc.itemsearch.search.query.elements

import com.mojang.brigadier.StringReader
import me.owdding.skyocean.features.misc.itemsearch.search.query.operators.LogicOperator
import me.owdding.skyocean.features.misc.itemsearch.search.query.operators.QueryOperator
import me.owdding.skyocean.utils.Utils.peekNext
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

open class SimpleLogicOperator(val op: String, val combine: (left: Predicate<ItemStack>, right: Predicate<ItemStack>) -> Predicate<ItemStack>) : LogicOperator {
    override fun apply(
        left: Predicate<ItemStack>,
        right: Predicate<ItemStack>,
    ) = combine(left, right)

    fun tryParse(stringReader: StringReader): QueryOperator? = this.takeIf { stringReader.canRead(op.length) && stringReader.peekNext(op.length) == op }

}

object AndOperator : SimpleLogicOperator("&&", { left, right -> left.and(right) })
object OrOperator : SimpleLogicOperator("||", { left, right -> left.or(right) })
