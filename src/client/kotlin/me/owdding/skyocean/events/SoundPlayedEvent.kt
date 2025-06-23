package me.owdding.skyocean.events

import net.minecraft.sounds.SoundEvent
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.CancellableSkyBlockEvent

// todo: sbapi
data class SoundPlayedEvent(val sound: SoundEvent, val pos: Vec3, val volume: Float, val pitch: Float) : CancellableSkyBlockEvent()
