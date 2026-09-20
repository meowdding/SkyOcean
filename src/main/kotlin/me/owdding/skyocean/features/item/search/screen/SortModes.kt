package me.owdding.skyocean.features.item.search.screen

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.features.item.sources.system.TrackedItem
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName

enum class SortModes(vararg additionalSortModes: AdditionalSortModes) : Comparator<TrackedItem>, Translatable {
    AMOUNT(AdditionalSortModes.AMOUNT, AdditionalSortModes.NAME, AdditionalSortModes.RARITY),
    PRICE(AdditionalSortModes.PRICE, AdditionalSortModes.NAME),
    RARITY(AdditionalSortModes.RARITY, AdditionalSortModes.AMOUNT, AdditionalSortModes.NAME),
    ;

    val comparator = additionalSortModes.map { it.comparator }.reduce { c1, c2 -> c1.thenComparing(c2) }
    override fun compare(o1: TrackedItem?, o2: TrackedItem?) = comparator.compare(o1, o2)
    override fun getTranslationKey(): String = "skyocean.screens.item_search.sort.${name.lowercase()}"
}

private enum class AdditionalSortModes(val comparator: Comparator<TrackedItem>) {
    NAME(Comparator.comparing { it.itemStack.cleanName }),
    AMOUNT(reversed(Comparator.comparingInt { it.itemStack.count })),
    RARITY(reversed(Comparator.comparingInt { (it.itemStack.getData(DataTypes.RARITY)?.ordinal ?: -1) })),
    PRICE(reversed(Comparator.comparing { it.price })),
}

private fun <T> reversed(comparator: Comparator<T>): Comparator<T> {
    return comparator.reversed()
}
