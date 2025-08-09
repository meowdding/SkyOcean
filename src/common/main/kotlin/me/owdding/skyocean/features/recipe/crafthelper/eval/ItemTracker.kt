package me.owdding.skyocean.features.recipe.crafthelper.eval

import me.owdding.lib.extensions.floor
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.recipe.CurrencyType
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.serialize
import tech.thatgravyboat.skyblockapi.api.area.farming.TrapperAPI
import tech.thatgravyboat.skyblockapi.api.area.hub.FarmhouseAPI
import tech.thatgravyboat.skyblockapi.api.area.rift.RiftAPI
import tech.thatgravyboat.skyblockapi.api.profile.CurrencyAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import kotlin.math.min

data class ItemTracker(val sources: Iterable<ItemSources> = ItemSources.entries) {
    constructor(vararg sources: ItemSources) : this(sources.toList())

    val currencies = mutableMapOf<CurrencyType, Number>(
        CurrencyType.COIN to (CurrencyAPI.bank + CurrencyAPI.purse).floor(),
        CurrencyType.BIT to CurrencyAPI.bits,
        CurrencyType.COPPER to CurrencyAPI.copper,
        CurrencyType.FOSSIL_DUST to 0,
        CurrencyType.BRONZE_MEDAL to FarmhouseAPI.bronzeMedals,
        CurrencyType.SILVER_MEDAL to FarmhouseAPI.silverMedals,
        CurrencyType.GOLD_MEDAL to FarmhouseAPI.goldMedals,
        CurrencyType.MOTE to RiftAPI.motes,
        CurrencyType.NORTH_STAR to CurrencyAPI.northStars,
        CurrencyType.PELT to TrapperAPI.pelts,
        CurrencyType.GEM to CurrencyAPI.gems,
    ).mapValues { (_, number) -> number.toInt() }.toMutableMap()

    val items = sources.mapNotNull { source -> source.itemSource?.getAll() }.flatten()
        .let { items ->

            buildList {
                addAll(items)
                addAll(sources.mapNotNull { it.itemSource?.postProcess(items) }.flatten())
            }
        }.mapNotNull { item ->
            item.itemStack.getSkyBlockId()?.let {
                TrackedItem(it, item.itemStack.count, item.context, item.context.source)
            }
        }.groupBy { it.id.lowercase() }
        .mapValues { (_, values) -> values.sortedBy { sourceToPriority(it.source) }.toMutableList() }.toMutableMap()


    fun sourceToPriority(source: ItemSources) = when (source) {
        ItemSources.INVENTORY -> 0
        ItemSources.SACKS -> 1
        ItemSources.STORAGE -> 2
        else -> 3
    }

    fun takeCurrency(amount: Int, currency: CurrencyType): Int {
        val amountPresent = currencies.getOrDefault(currency, 0)
        val min = min(amount, amountPresent)
        currencies[currency] = amountPresent - min
        return min
    }


    fun takeN(ingredient: Ingredient, amount: Int = ingredient.amount): List<TrackedItem> {
        val items = items[ingredient.serialize().lowercase()] ?: return emptyList()

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
        if (items.isEmpty()) this.items.remove(ingredient.serialize().lowercase())

        if (takeWhile.sumOf { it.amount } <= amount) return takeWhile

        val list = mutableListOf<TrackedItem>()

        val last = takeWhile.lastOrNull() ?: return takeWhile

        val other = takeWhile.dropLast(1)
        val otherSum = other.sumOf { it.amount }

        list.addAll(other)

        val lastNeeded = (amount - otherSum).coerceAtMost(amount)

        items.addFirst(last.withAmount(last.amount - lastNeeded))
        if (items.size == 1) {
            this.items.put(ingredient.serialize().lowercase(), items)
        }

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
