package me.owdding.skyocean.features.misc.itemsearch.search

import me.owdding.skyocean.features.misc.itemsearch.item.TrackedItemBundle
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData

class BundleItemFilter(
    bundle: TrackedItemBundle,
) : ItemFilter {

    val reference = bundle.itemStack

    val id = reference.getData(DataTypes.API_ID)
    val attributes = reference.getData(DataTypes.ATTRIBUTES)
    val enchants = reference.getData(DataTypes.ENCHANTMENTS)
    val modifier = reference.getData(DataTypes.MODIFIER)

    override fun test(other: ItemStack): Boolean {
        id ?: return false
        if (other === reference) return true
        if (id != other.getData(DataTypes.API_ID)) return false

        if (attributes != null) {
            val otherAttributes = other.getData(DataTypes.ATTRIBUTES) ?: return false
            if (!compareMap(attributes, otherAttributes)) return false
        }

        if (enchants != null) {
            val otherEnchants = other.getData(DataTypes.ENCHANTMENTS) ?: return false
            if (!compareMap(enchants, otherEnchants)) return false
        }

        if (modifier != null) {
            val otherModifier = other.getData(DataTypes.MODIFIER) ?: return false
            if (modifier != otherModifier) return false
        }

        return true
    }

    fun compareMap(first: Map<*, *>, second: Map<*, *>): Boolean {
        for (firstMapKey in first.keys) {
            if (!second.containsKey(firstMapKey)) {
                return false
            }
            if (first[firstMapKey] != second[firstMapKey]) {
                return false
            }
        }

        return true
    }


}
