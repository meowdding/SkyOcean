package me.owdding.skyocean.features.recipe.crafthelper.eval

import me.owdding.lib.extensions.floor
import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.recipe.crafthelper.Ingredient
import me.owdding.skyocean.features.recipe.crafthelper.serialize
import tech.thatgravyboat.skyblockapi.api.profile.CurrencyAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import kotlin.math.min

data class ItemTracker(val sources: Iterable<ItemSources> = ItemSources.entries) {
    constructor(vararg sources: ItemSources) : this(sources.toList())

    var coins = (CurrencyAPI.bank + CurrencyAPI.purse).floor()

    val items = sources.mapNotNull { source -> source.itemSource?.getAll() }.flatten()
        .mapNotNull { item -> item.itemStack.getSkyBlockId()?.let { TrackedItem(it, item.itemStack.count, item.context, item.context.source) } }
        .groupBy { it.id }
        .mapValues { (_, values) -> values.sortedBy { sourceToPriority(it.source) }.toMutableList() }.toMutableMap()


    fun sourceToPriority(source: ItemSources) = when (source) {
        ItemSources.INVENTORY -> 0
        ItemSources.SACKS -> 1
        ItemSources.STORAGE -> 2
        else -> 3
    }

    fun takeCoins(amount: Int): Int {
        val min = min(amount, coins)
        coins -= min
        return min
    }

    fun takeN(ingredient: Ingredient, amount: Int = ingredient.amount): List<TrackedItem> {
        val items = items[ingredient.serialize()] ?: return emptyList()


        var acc = 0
        val takeWhile = items.takeWhile {
            val shouldTake = acc < amount
            acc += it.amount
            shouldTake
        }
        if (takeWhile.isEmpty()) {
            return emptyList()
        }

        items.removeAll(takeWhile)
        if (items.isEmpty()) this.items.remove(ingredient.serialize())

        if (takeWhile.sumOf { it.amount } <= amount) return takeWhile

        val list = mutableListOf<TrackedItem>()

        val last = takeWhile.lastOrNull() ?: return takeWhile

        val other = takeWhile.dropLast(1)
        val otherSum = other.sumOf { it.amount }

        list.addAll(other)

        val lastNeeded = (amount - otherSum).coerceAtMost(amount)

        items.addFirst(last.withAmount(last.amount - lastNeeded))

        list.add(last.withAmount(lastNeeded))

        return list
    }
}

data class TrackedItem(
    val id: String,
    val amount: Int,
    val context: ItemContext,
    val source: ItemSources,
) {
    fun withAmount(amount: Int) = copy(amount = amount)
}
