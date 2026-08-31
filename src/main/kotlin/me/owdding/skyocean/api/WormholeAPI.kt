package me.owdding.skyocean.api

import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.fishing.FishCatchEvent
import me.owdding.skyocean.events.fishing.WormholeEvent
import me.owdding.skyocean.features.fishing.WormholeFeatures
import me.owdding.skyocean.utils.Utils.roundToHalf
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import org.joml.Vector3f
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.PacketReceivedEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import kotlin.math.abs
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Module
object WormholeAPI {

    private val _wormholes = mutableListOf<WormholeData>()
    val wormholes: List<WormholeData> get() = _wormholes

    var lastWormholeFish = Instant.DISTANT_PAST
        private set

    private const val MAX_RADIUS = 9 // squared
    private val islands = listOf(SkyBlockIsland.LOTUS_ATOLL, SkyBlockIsland.CRIMSON_ISLE)

    @Subscription
    fun onCatch(event: FishCatchEvent) {
        val hookY = event.hookPos.y
        val hookPosD = event.hookPos.toVector3f()

        _wormholes.filter {
            val y = it.pos?.y ?: return@filter false
            abs(hookY - y) < 3
        }.minByOrNull { it.pos?.distanceSquared(hookPosD) ?: Float.MAX_VALUE }?.let {
            it.fishedIn = true
            lastWormholeFish = currentInstant()
        }
    }

    @Subscription(ServerChangeEvent::class)
    fun onServerChange() {
        _wormholes.clear()
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onParticle(event: PacketReceivedEvent) {
        if (!inWormholeIsland()) return

        val packet = event.packet as? ClientboundLevelParticlesPacket ?: return
        if (!packet.isHotSpotParticle()) return

        val iterator = _wormholes.iterator()
        while (iterator.hasNext()) {
            val wormhole = iterator.next()
            if (wormhole.lastParticleTime.since() > 3.seconds) {
                iterator.remove()
                WormholeEvent.Despawn(wormhole).post(SkyBlockAPI.eventBus)
            }
        }

        val searchRadius = MAX_RADIUS * 2

        val closestMatch = _wormholes.asSequence().mapNotNull { entry ->
            val pos = entry.pos ?: return@mapNotNull null
            val distanceSq = (packet.x - pos.x).pow(2) + (packet.z - pos.z).pow(2)

            if (distanceSq > searchRadius) return@mapNotNull null
            entry to distanceSq
        }.minByOrNull { it.second }?.first

        val isNew = closestMatch == null
        val wormhole = closestMatch ?: WormholeData().also { _wormholes.add(it) }

        wormhole.addParticle(packet.x, packet.y, packet.z)

        if (isNew) {
            WormholeEvent.Spawn(wormhole).post(SkyBlockAPI.eventBus)
        }

        if (WormholeFeatures.shouldHideParticles()) event.cancel()
    }

    private fun ClientboundLevelParticlesPacket.isHotSpotParticle(): Boolean {
        return when (this.particle.type) {
            ParticleTypes.ENCHANT -> this.count == 4 && this.maxSpeed == -1.2f
            ParticleTypes.PORTAL -> this.count == 5 && this.maxSpeed == 0.25f
            else -> false
        }
    }

    fun inWormholeIsland() = SkyBlockIsland.inAnyIsland(islands)
}

data class WormholeData(
    var pos: Vector3f? = null,
    var radius: Double? = null,
    var fishedIn: Boolean = false
) {
    var lastParticleTime: Instant = currentInstant()

    private var minX: Double = Double.MAX_VALUE
    private var maxX: Double = -Double.MAX_VALUE
    private var minZ: Double = Double.MAX_VALUE
    private var maxZ: Double = -Double.MAX_VALUE

    fun addParticle(x: Double, y: Double, z: Double) {
        lastParticleTime = currentInstant()

        if (x < minX) minX = x
        if (x > maxX) maxX = x
        if (z < minZ) minZ = z
        if (z > maxZ) maxZ = z

        val centerX = (minX + maxX) / 2.0
        val centerZ = (minZ + maxZ) / 2.0
        this.pos = Vector3f(centerX.toFloat(), y.toFloat(), centerZ.toFloat())

        this.radius = ((maxX - minX) / 2.0).roundToHalf() // Hopefully good enough and we don't need to consider Z direction
    }
}
