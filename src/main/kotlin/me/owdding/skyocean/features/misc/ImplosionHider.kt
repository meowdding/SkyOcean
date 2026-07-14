package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.MiscConfig
import net.minecraft.client.particle.HugeExplosionParticle
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.level.ParticleEmitEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue

@Module
object ImplosionHider {

    private val witherBladeIds = setOf("HYPERION", "ASTRAEA", "VALKYRIE", "SCYLLA")
    private val players: MutableSet<Vec3> = ConcurrentHashMap.newKeySet()

    fun Vec3.isInRange(event: ParticleEmitEvent) = this.distanceToSqr(event.particle.x, event.particle.y, event.particle.z) <= 4.0

    @Subscription
    @OnlyOnSkyBlock
    fun onParticle(event: ParticleEmitEvent) {
        if (!MiscConfig.hideImplosions) return
        val self = McPlayer.self ?: return
        val particle = event.particle as? HugeExplosionParticle ?: return
        if (particle.getQuadSize(1f).absoluteValue <= 25 && (self.position().isInRange(event) || players.any { player -> player.isInRange(event) })) {
            event.cancel()
        }
    }

    @TimePassed("2t")
    @Subscription(TickEvent::class)
    @OnlyOnSkyBlock
    fun tick() {
        if (!MiscConfig.hideImplosions) return
        players.clear()
        McLevel.selfOrNull?.players()?.filter {
            it.mainHandItem.getData(DataTypes.ID) in witherBladeIds || it == McPlayer.self
        }?.forEach { players.add(it.position()) }
    }

}
