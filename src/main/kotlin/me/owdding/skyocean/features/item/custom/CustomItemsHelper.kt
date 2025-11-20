package me.owdding.skyocean.features.item.custom

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import me.owdding.skyocean.features.item.custom.CustomItems.get
import me.owdding.skyocean.features.item.custom.CustomItems.getCustomData
import me.owdding.skyocean.features.item.custom.CustomItems.getStaticCustomData
import me.owdding.skyocean.features.item.custom.CustomItems.getVanillaIntegrationData
import me.owdding.skyocean.features.item.custom.data.CustomItemData
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents
import me.owdding.skyocean.features.item.custom.data.EquippableModelState
import me.owdding.skyocean.utils.Utils.unsafeCast
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.TriState
import net.minecraft.world.item.ItemStack

object CustomItemsHelper {

    @JvmStatic
    fun <T> getData(instance: ItemStack, type: DataComponentType<T>): T? = context(instance) { getCustomData(instance)?.getData(type) }

    fun getCustomData(instance: ItemStack) = instance.getStaticCustomData() ?: instance.getCustomData() ?: instance.getVanillaIntegrationData()

    fun getNameReplacement(stack: ItemStack): Component? = stack[CustomItemDataComponents.NAME]

    @JvmStatic
    fun getEquippableState(instance: ItemStack): EquippableModelState {
        val model = getCustomData(instance)?.get(CustomItemDataComponents.MODEL) ?: return EquippableModelState.VANILLA
        val modelEquippable = model.resolveToItem()?.components()[DataComponents.EQUIPPABLE] ?: return EquippableModelState.NON_EQUIPPABLE
        return EquippableModelState(TriState.TRUE, modelEquippable)
    }

    context(item: ItemStack) fun <T> CustomItemData.getData(type: DataComponentType<T>): T? = when (type) {
        DataComponents.ITEM_MODEL -> this[CustomItemDataComponents.MODEL]?.getModel()
        DataComponents.CUSTOM_NAME -> this[CustomItemDataComponents.NAME]
        DataComponents.ENCHANTMENT_GLINT_OVERRIDE -> this[CustomItemDataComponents.ENCHANTMENT_GLINT_OVERRIDE]
        DataComponents.TRIM -> this[CustomItemDataComponents.ARMOR_TRIM]?.trim
        DataComponents.PROFILE -> this[CustomItemDataComponents.SKIN]?.getResolvableProfile()
        DataComponents.DYED_COLOR -> this[CustomItemDataComponents.COLOR]?.getDyeColor(item)
        DataComponents.EQUIPPABLE -> this[CustomItemDataComponents.MODEL]?.resolveToItem()?.components()[DataComponents.EQUIPPABLE]
        else -> null
    }.unsafeCast()

    @JvmStatic
    fun <T> replace(itemStack: ItemStack, type: DataComponentType<T>, original: Operation<T>): T {
        return getData(itemStack, type) ?: original.call(itemStack, type)
    }

    @JvmStatic
    fun getColor(itemStack: ItemStack) = getCustomData(itemStack)?.let { it[CustomItemDataComponents.COLOR]?.getColor(itemStack) }

}
