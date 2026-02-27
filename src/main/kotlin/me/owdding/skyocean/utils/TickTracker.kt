package me.owdding.skyocean.utils

import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent

class TickTracker {
    var lastTick = TickEvent.ticks

    fun consume(): Boolean {
        if (lastTick != TickEvent.ticks) {
            lastTick = TickEvent.ticks
            return true
        }
        return false
    }
}
