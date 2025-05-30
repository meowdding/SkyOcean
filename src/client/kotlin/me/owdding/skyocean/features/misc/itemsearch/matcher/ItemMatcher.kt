package me.owdding.skyocean.features.misc.itemsearch.matcher

import com.teamresourceful.resourcefullib.common.utils.TriState
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataType
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData

object ItemMatcher {

    fun compare(first: ItemStack, second: ItemStack): TriState {
        fun match(dataType: DataType<*>): Boolean {
            val firstData = first.getData(dataType)
            val secondData = second.getData(dataType)

            if (firstData == null && secondData == null) return false

            if (firstData is Map<*, *> && secondData is Map<*, *>) {
                return !firstData.all { (key, value) ->
                    secondData.containsKey(key) && secondData[key] == value
                }
            }

            if (firstData == secondData) return false

            return true
        }

        return when {
            match(DataTypes.UUID) -> TriState.FALSE
            match(DataTypes.TIMESTAMP) -> TriState.of(!match(DataTypes.API_ID))
            match(DataTypes.API_ID) -> TriState.UNDEFINED
            match(DataTypes.ENCHANTMENTS) -> TriState.UNDEFINED
            match(DataTypes.ATTRIBUTES) -> TriState.UNDEFINED
            else -> TriState.UNDEFINED
        }
    }


}
