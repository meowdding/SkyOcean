package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.events.ParticleEmitEvent
import net.minecraft.client.particle.HugeExplosionParticle
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import kotlin.math.absoluteValue

@Module
object ImplosionHider {

    private val players: MutableSet<Vec3> = mutableSetOf()

    @Subscription
    fun onParticle(event: ParticleEmitEvent) {
        if (!MiscConfig.hideImplosions) return
        val self = McPlayer.self ?: return
        if (event.particle !is HugeExplosionParticle) return
        if (event.particle.getQuadSize(1f).absoluteValue <= 25 && (self.distanceToSqr(
                event.particle.x,
                event.particle.y,
                event.particle.z,
            ) <= 4.0 || players.any { player ->
                player.distanceToSqr(
                    event.particle.x,
                    event.particle.y,
                    event.particle.z,
                ) <= 4.0
            })
        ) {
            event.cancel()
        }
    }

    @TimePassed("2t")
    @Subscription(TickEvent::class)
    fun tick() {
        if (!MiscConfig.hideImplosions) return
        players.clear()
        McLevel.players.filter {
            it.mainHandItem.getData(DataTypes.NECRON_SCROLLS) != null && it == McPlayer.self
        }.forEach { players.add(it.position()) }
    }

}
