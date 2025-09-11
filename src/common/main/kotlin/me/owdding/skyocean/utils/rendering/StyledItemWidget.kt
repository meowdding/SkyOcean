package me.owdding.skyocean.utils.rendering

import earth.terrarium.olympus.client.components.base.BaseWidget
import net.minecraft.world.item.ItemStack

expect class StyledItemWidget : BaseWidget {

    constructor(stack: ItemStack)
}
