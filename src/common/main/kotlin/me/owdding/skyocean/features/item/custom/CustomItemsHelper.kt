package me.owdding.skyocean.features.item.custom

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import me.owdding.skyocean.features.item.custom.CustomItems.get
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents
import me.owdding.skyocean.utils.Utils.unsafe
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object CustomItemsHelper {

    @JvmStatic
    fun getNameReplacement(stack: ItemStack): Component? = stack[CustomItemDataComponents.NAME]

    @JvmStatic
    fun <T> getData(instance: ItemStack, type: DataComponentType<T>): T? = when (type) {
        DataComponents.ITEM_MODEL -> instance[CustomItemDataComponents.MODEL]?.getModel()
        DataComponents.CUSTOM_NAME -> instance[CustomItemDataComponents.NAME]
        DataComponents.ENCHANTMENT_GLINT_OVERRIDE -> instance[CustomItemDataComponents.ENCHANTMENT_GLINT_OVERRIDE]
        DataComponents.TRIM -> instance[CustomItemDataComponents.ARMOR_TRIM]?.trim
        DataComponents.PROFILE -> instance[CustomItemDataComponents.SKIN]?.getResolvableProfile()
        DataComponents.DYED_COLOR -> instance[CustomItemDataComponents.COLOR]?.getDyeColor()
        else -> null
    }.unsafe()

    @JvmStatic
    fun <T> replace(itemStack: ItemStack, type: DataComponentType<T>, original: Operation<T>): T {
        return getData(itemStack, type) ?: original.call(itemStack, type)
    }

}
