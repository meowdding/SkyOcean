package me.owdding.skyocean.features.item.search.screen

import me.owdding.skyocean.features.item.sources.system.TrackedItem
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName

enum class SortModes(vararg additionalSortModes: AdditionalSortModes) : Comparator<TrackedItem> {
    AMOUNT(AdditionalSortModes.AMOUNT, AdditionalSortModes.NAME, AdditionalSortModes.RARITY),
    PRICE(AdditionalSortModes.PRICE, AdditionalSortModes.NAME),
    RARITY(AdditionalSortModes.RARITY, AdditionalSortModes.AMOUNT, AdditionalSortModes.NAME),
    ;

    val comparator = additionalSortModes.map { it.comparator }.reduce { c1, c2 -> c1.thenComparing(c2) }
    override fun compare(o1: TrackedItem?, o2: TrackedItem?) = comparator.compare(o1?.itemStack, o2?.itemStack)
}

private enum class AdditionalSortModes(val comparator: Comparator<ItemStack>) {
    NAME(Comparator.comparing { it.cleanName }),
    AMOUNT(reversed(Comparator.comparingInt { it.count })),
    RARITY(reversed(Comparator.comparingInt { (it.getData(DataTypes.RARITY)?.ordinal ?: -1) })),
    PRICE(reversed(Comparator.comparing { it.getItemValue().price })),
}

private fun <T> reversed(comparator: Comparator<T>): Comparator<T> {
    return comparator.reversed()
}
