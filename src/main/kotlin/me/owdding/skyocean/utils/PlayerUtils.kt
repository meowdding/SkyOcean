package me.owdding.skyocean.utils

import me.owdding.ktmodules.Module
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import kotlin.time.Clock
import kotlin.time.Instant

@Module
object PlayerUtils {

    var lastPos: MutableMap<Player, Vec3> = mutableMapOf()
        private set
    var lastMoveTime: MutableMap<Player, Instant> = mutableMapOf()
        private set

    @Subscription(TickEvent::class)
    fun onTick() {
        McLevel.players.forEach {
            updatePlayerMovement(it)
        }
    }

    private fun updatePlayerMovement(player: Player) {
        val currentPos = player.position()
        val lastPlayerPos = lastPos[player]
        if (lastPlayerPos == null || currentPos != lastPlayerPos) {
            lastPos[player] = currentPos
            lastMoveTime[player] = Clock.System.now()
        }
    }

}
