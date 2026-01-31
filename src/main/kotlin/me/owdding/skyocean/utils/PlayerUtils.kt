package me.owdding.skyocean.utils

import me.owdding.ktmodules.Module
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityRemovedEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import java.util.UUID
import kotlin.time.Instant

@Module
object PlayerUtils {

    private var lastPos: MutableMap<UUID, Vec3> = mutableMapOf()
    private var lastMoveTime: MutableMap<UUID, Instant> = mutableMapOf()

    fun getLastPos(uuid: UUID): Vec3? = lastPos[uuid]
    fun getLastMoveTime(uuid: UUID): Instant? = lastMoveTime[uuid]

    @Subscription(TickEvent::class)
    fun onTick() {
        McLevel.players.forEach {
            updatePlayerMovement(it)
        }
    }

    @Subscription
    fun onEntityRemove(event: EntityRemovedEvent) {
        val player = event.entity as? Player ?: return
        lastPos.remove(player.uuid)
        lastMoveTime.remove(player.uuid)
    }

    private fun updatePlayerMovement(player: Player) {
        val currentPos = player.position()
        val lastPlayerPos = lastPos[player.uuid]
        if (lastPlayerPos == null || currentPos != lastPlayerPos) {
            lastPos[player.uuid] = currentPos
            lastMoveTime[player.uuid] = currentInstant()
        }
    }

}
