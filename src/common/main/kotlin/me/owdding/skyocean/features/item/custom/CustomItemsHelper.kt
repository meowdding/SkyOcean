package me.owdding.skyocean.features.item.custom

import me.owdding.skyocean.features.item.custom.CustomItems.get
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object CustomItemsHelper {

    @JvmStatic
    fun getNameReplacement(stack: ItemStack): Component? = stack[CustomItemDataComponents.NAME]

}
