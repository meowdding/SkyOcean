package me.owdding.skyocean.features.item.search.search

import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData

data class ExactMatchItemFilter(
    val reference: ItemStack,
) : ItemFilter {

    val uuid = reference.getData(DataTypes.UUID)
    val timestamp = reference.getData(DataTypes.TIMESTAMP)
    val id = reference.getData(DataTypes.API_ID)

    override fun test(other: ItemStack): Boolean {
        id ?: return false
        if (other === reference) return true
        if (id != other.getData(DataTypes.API_ID)) return false

        if (uuid != null) return uuid == other.getData(DataTypes.UUID)
        if (timestamp != null) return timestamp == other.getData(DataTypes.TIMESTAMP)

        return false
    }
}
