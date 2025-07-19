package me.owdding.skyocean.events

import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent

@ItemSearchComponent
data class ItemStackCreateEvent(
    val itemStack: ItemStack,
) : SkyBlockEvent()

@RequiresOptIn("This event should only be used for code related to the item search feature!")
annotation class ItemSearchComponent
