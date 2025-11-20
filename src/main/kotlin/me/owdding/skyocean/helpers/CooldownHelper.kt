package me.owdding.skyocean.helpers

import me.owdding.ktmodules.Module
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration
import kotlin.time.Instant

class CooldownHelper(
    val cooldown: () -> Duration,
    val onReady: () -> Unit,
) {
    constructor(cooldown: Duration, onReady: () -> Unit) : this({ cooldown }, onReady)

    private var lastUsed = Instant.DISTANT_PAST

    init {
        cooldowns.add(this)
    }

    fun setNow() {
        lastUsed = currentInstant()
    }

    fun reset() {
        lastUsed = Instant.DISTANT_PAST
    }

    @Module
    companion object {
        private val cooldowns = mutableListOf<CooldownHelper>()

        @Subscription(TickEvent::class)
        @TimePassed("5s")
        fun onTick() {
            cooldowns.forEach { helper ->
                if (helper.lastUsed.since() >= helper.cooldown()) {
                    helper.onReady()
                    helper.setNow()
                }
            }
        }
    }
}
