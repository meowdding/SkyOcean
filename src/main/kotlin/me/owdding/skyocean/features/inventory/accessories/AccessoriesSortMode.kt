package me.owdding.skyocean.features.inventory.accessories

import me.owdding.skyocean.utils.extensions.getRealRarity
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName

enum class AccessoriesSortMode(vararg sortModes: Comparator<TrackedAccessory>, displayName: String? = null) : Comparator<TrackedAccessory> {
    AP(Mode.AP, Mode.PRICE, Mode.RARITY, displayName = "AP"),
    PRICE(Mode.PRICE, Mode.AP, Mode.RARITY),
    RARITY(Mode.RARITY, Mode.PRICE, Mode.AP),
    PRICE_PER_AP(Mode.PRICE_PER_AP, Mode.RARITY, displayName = "Price per AP"),
    ;

    val displayName: String = displayName ?: toFormattedName()
    val comparator: Comparator<TrackedAccessory> = sortModes.reduce { c1, c2 -> c1.thenComparing(c2) }
    override fun compare(o1: TrackedAccessory?, o2: TrackedAccessory?): Int = comparator.compare(o1, o2)

    companion object {
        val BASE: Comparator<TrackedAccessory> = Mode.MARKED.thenComparing(Mode.HAS_PRICE)
        fun hasPrice(accessory: TrackedAccessory): Boolean = AdditionalSortMode.getPrice(accessory) != Long.MAX_VALUE
    }
}

private typealias Mode = AdditionalSortMode

private enum class AdditionalSortMode(comparator: Comparator<TrackedAccessory>) : Comparator<TrackedAccessory> by comparator {
    MARKED(reversed(Comparator.comparing { it.marked })),
    HAS_PRICE(reversed(Comparator.comparing { getPrice(it) != Long.MAX_VALUE })),
    AP(reversed(Comparator.comparing(::getAp))),
    PRICE(Comparator.comparing { getPrice(it) }),
    RARITY(reversed(Comparator.comparing { getRarity(it) })),
    PRICE_PER_AP(Comparator.comparing { getPrice(it) / getAp(it) }),
    ;

    companion object {
        fun getPrice(accessory: TrackedAccessory): Long = accessory.items.minOf { item -> item.getItemValue().rawPrice.takeIf { it > 0 } ?: Long.MAX_VALUE }
        // TODO: handle accessory upgrades of recombed accessories
        private fun getAp(accessory: TrackedAccessory): Int {
            return AccessoriesAPI.getAp(accessory.items.first())
        }
        // TODO: handle accessory upgrades of recombed accessories?
        private fun getRarity(accessory: TrackedAccessory): SkyBlockRarity {
            val item = accessory.items.first()
            val realRarity = item.getRealRarity() ?: return COMMON
            return realRarity
        }
    }
}

private fun <T> reversed(comparator: Comparator<T>): Comparator<T> {
    return comparator.reversed()
}
