package me.owdding.skyocean.events

import net.minecraft.client.particle.Particle
import tech.thatgravyboat.skyblockapi.api.events.base.CancellableSkyBlockEvent

data class ParticleEmitEvent(val particle: Particle) : CancellableSkyBlockEvent()
