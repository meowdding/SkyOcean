package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent

@Module
object AnvilHelper {

    val slots = intArrayOf(29, 33)

    @Subscription
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (event.title != "Anvil") return

    }

}
