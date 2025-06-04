package me.owdding.skyocean.features.misc.itemsearch.screen

import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName

enum class SortModes(private vararg val additionalSortModes: AdditionalSortModes) {

    AMOUNT(AdditionalSortModes.AMOUNT, AdditionalSortModes.NAME, AdditionalSortModes.RARITY),
    PRICE(AdditionalSortModes.PRICE, AdditionalSortModes.NAME),
    RARITY(AdditionalSortModes.RARITY, AdditionalSortModes.AMOUNT, AdditionalSortModes.NAME),
    MEOW,
    ;

    val comparator = additionalSortModes.map { it.comparator }.reduce { c1, c2 -> c1.thenComparing(c2) }
}

private enum class AdditionalSortModes(val comparator: Comparator<ItemStack>) {
    NAME(Comparator.comparing { it.cleanName }),
    AMOUNT(re(Comparator.comparingInt { it.count })),
    RARITY(re(Comparator.comparingInt { (it.getData(DataTypes.RARITY)?.ordinal ?: -1) })),
    PRICE(re(Comparator.comparing { it.getItemValue().price })),
    ;

}

fun <T> re(comparator: Comparator<T>): Comparator<T> {
    return comparator.reversed()
}
