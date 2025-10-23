package me.owdding.skyocean.features.item.custom

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import me.owdding.skyocean.features.item.custom.CustomItems.get
import me.owdding.skyocean.features.item.custom.CustomItems.getCustomData
import me.owdding.skyocean.features.item.custom.CustomItems.getKey
import me.owdding.skyocean.features.item.custom.CustomItems.getStaticCustomData
import me.owdding.skyocean.features.item.custom.CustomItems.getVanillaIntegrationData
import me.owdding.skyocean.features.item.custom.data.CustomItemData
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents
import me.owdding.skyocean.features.item.custom.data.IdKey
import me.owdding.skyocean.features.item.custom.ui.standard.StandardCustomizationUi
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.Utils.unsafeCast
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object CustomItemsHelper {

    @JvmStatic
    fun <T> getData(instance: ItemStack, type: DataComponentType<T>): T? = context(instance) { getCustomData(instance)?.getData(type) }

    fun getCustomData(instance: ItemStack) = instance.getStaticCustomData() ?: instance.getCustomData() ?: instance.getVanillaIntegrationData()

    fun getNameReplacement(stack: ItemStack): Component? = stack[CustomItemDataComponents.NAME]

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

    fun tryAndOpenCustomizationUi(item: ItemStack) {
        if (item.getKey() == null) {
            text {
                append(item.hoverName)
                append(" can't be customized!")
            }.sendWithPrefix()
            return
        }

        if (item.getKey() is IdKey) {
            text {
                append("Modification will be visible on all variants of this item!")
                this.color = OceanColors.WARNING
            }.sendWithPrefix()
        }

        McClient.runNextTick {
            StandardCustomizationUi.open(item)
        }
    }
}
