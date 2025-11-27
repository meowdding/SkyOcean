package me.owdding.skyocean.features.inventory.accessories

import me.owdding.skyocean.utils.Utils.getRealRarity
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue

// TODO: figure out the best order for some of these
enum class AccessoriesSortMode(vararg sortModes: Mode) : Comparator<TrackedAccessory> {
    MP(Mode.MARKED, Mode.MP, Mode.PRICE, Mode.RARITY),
    PRICE(Mode.MARKED, Mode.PRICE, Mode.MP, Mode.RARITY),
    RARITY(Mode.MARKED, Mode.RARITY, Mode.PRICE, Mode.MP),
    PRICE_PER_MP(Mode.MARKED, Mode.PRICE_PER_MP, Mode.RARITY),
    ;

    val comparator = sortModes.map { it.comparator }.reduce { c1, c2 -> c1.thenComparing(c2) }
    override fun compare(o1: TrackedAccessory?, o2: TrackedAccessory?): Int = comparator.compare(o1, o2)
}

private typealias Mode = AdditionalSortMode

private enum class AdditionalSortMode(val comparator: Comparator<TrackedAccessory>) {
    MARKED(reversed(Comparator.comparing { it.marked })),
    MP(reversed(Comparator.comparing { AccessoriesAPI.getMp(it.items.first()) })),
    PRICE(Comparator.comparing { getPrice(it) }),
    RARITY(reversed(Comparator.comparing { getRarity(it) })),
    PRICE_PER_MP(reversed(Comparator.comparing { getPrice(it) / getMp(it) })),
    ;

    companion object {
        private fun getPrice(accessory: TrackedAccessory): Long = accessory.items.minOf { item -> item.getItemValue().rawPrice.takeIf { it > 0 } ?: Long.MAX_VALUE }
        // TODO: handle accessory upgrades of recombed accessories
        private fun getMp(accessory: TrackedAccessory): Int {
            return AccessoriesAPI.getMp(accessory.items.first())
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
