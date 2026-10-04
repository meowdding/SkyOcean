package me.owdding.skyocean.features.item.sources.system

import me.owdding.skyocean.config.features.misc.MiscConfig
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.ItemData
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId

data class SimpleTrackedItem(
    override val itemStack: ItemStack,
    override val context: ItemContext,
) : TrackedItem {
    override val price
        get() = when (MiscConfig.priceSource) {
            BAZAAR -> itemStack.getItemValue().price
            NPC -> (ItemData.getNpcSellPrice(itemStack.getSkyBlockId() ?: "") ?: 0).toLong()
        }

    override fun add(other: TrackedItem) = TrackedItemBundle(this).apply { this.add(other) }
}
