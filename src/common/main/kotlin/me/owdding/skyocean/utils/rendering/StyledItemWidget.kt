package me.owdding.skyocean.utils.rendering

import earth.terrarium.olympus.client.components.base.BaseWidget
import net.minecraft.world.item.ItemStack
import net.msrandom.stub.Stub

@Stub
expect class StyledItemWidget : BaseWidget {
    constructor(stack: ItemStack)
}
