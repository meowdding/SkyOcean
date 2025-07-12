package me.owdding.skyocean.features.item.search.matcher

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataType
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

object ItemMatcher {

    fun compare(first: ItemStack, second: ItemStack): Boolean {
        if (first.item !== second.item) {
            return false
        }
        if (!isSame(first, second, DataTypes.API_ID)) {
            return false
        }
        if (!isSame(first, second, DataTypes.ENCHANTMENTS)) {
            return false
        }
        if (!isSame(first, second, DataTypes.ATTRIBUTES)) {
            return false
        }
        if (!isSame(first, second, DataComponents.CUSTOM_NAME)) {
            return false
        }
        return isSame(first, second, DataTypes.MODIFIER)
    }

    fun <T> dataType(type: DataType<T>): (ItemStack) -> T? = { it.getData(type) }
    fun <T> component(type: DataComponentType<T>): (ItemStack) -> T? = { it.get(type) }
    fun <T> isSame(first: ItemStack, second: ItemStack, type: DataType<T>) = isSame(first, second, dataType(type))
    fun <T> isSame(first: ItemStack, second: ItemStack, type: DataComponentType<T>) = isSame(first, second, component(type))
    fun <T> isSame(first: ItemStack, second: ItemStack, dataGetter: (ItemStack) -> T?): Boolean {
        val firstComponent: T? = dataGetter(first)
        val secondComponent: T? = dataGetter(second)

        if (firstComponent == null || secondComponent == null) {
            return firstComponent == null && secondComponent == null
        }

        if (firstComponent is MutableMap<*, *> && secondComponent is MutableMap<*, *>) {
            for (firstMapKey in firstComponent.keys) {
                if (!secondComponent.containsKey(firstMapKey)) {
                    return false
                }
                if (secondComponent[firstMapKey] != firstComponent[firstMapKey]) {
                    return false
                }
            }
        } else if (firstComponent is Component && secondComponent is Component) {
            return firstComponent.stripped.equals(secondComponent.stripped, true)
        }

        return firstComponent == secondComponent
    }

}
