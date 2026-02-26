package me.owdding.skyocean.utils

import me.owdding.ktmodules.Module
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityRemovedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.PacketReceivedEvent
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import java.util.*
import kotlin.time.Instant

@Module
object PlayerUtils {

    private val lastMoveTime: MutableMap<UUID, Instant> = mutableMapOf()

    fun getLastMoveTime(uuid: UUID): Instant? = lastMoveTime[uuid]

    @Subscription
    fun onPacket(event: PacketReceivedEvent) {
        val packet = event.packet as? ClientboundPlayerPositionPacket ?: return
        val player = McLevel.level.getEntity(packet.id) as? Player ?: return

        val delta = packet.change.deltaMovement
        updatePlayerMovement(player, delta)
    }

    @Subscription
    fun onEntityRemove(event: EntityRemovedEvent) {
        val player = event.entity as? Player ?: return
        lastMoveTime.remove(player.uuid)
    }

    @Subscription(ServerChangeEvent::class)
    fun onWorldChange() {
        lastMoveTime.clear()
    }

    private fun updatePlayerMovement(player: Player, delta: Vec3) {
        if (delta != Vec3.ZERO) {
            lastMoveTime[player.uuid] = currentInstant()
        }
    }

}
