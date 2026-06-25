package me.owdding.skyocean.helpers

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import kotlin.time.Duration
import kotlin.time.Instant

class CooldownHelper(
    val cooldown: () -> Duration,
    val onReady: () -> Unit,
    val busyOption: () -> Boolean = { false },
) {
    constructor(cooldown: Duration, onReady: () -> Unit, busyOption: () -> Boolean = { false }) : this({ cooldown }, onReady, busyOption)

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
                    if (helper.busyOption() && isBusy()) {
                        return@forEach
                    }
                    helper.onReady()
                    helper.setNow()
                }
            }
        }

        private fun isBusy() = when {
            SkyBlockIsland.DUNGEON_HUB.inIsland() -> !DungeonAPI.completed
            SkyBlockIsland.MINESHAFT.inIsland() -> true
            SkyBlockIsland.KUUDRA.inIsland() -> true
            SkyBlockIsland.DARK_AUCTION.inIsland() -> true
            SkyBlockIsland.THE_RIFT.inIsland() -> true
            else -> false
        }
    }
}
