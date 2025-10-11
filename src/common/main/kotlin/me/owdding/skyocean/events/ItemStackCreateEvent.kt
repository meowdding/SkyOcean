package me.owdding.skyocean.events

import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent

data class ItemStackCreateEvent(
    val itemStack: ItemStack,
) : SkyBlockEvent()
