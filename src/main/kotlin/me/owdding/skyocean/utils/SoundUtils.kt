package me.owdding.skyocean.utils

import me.owdding.ktmodules.Module
import net.minecraft.sounds.SoundEvent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient

@Module
object SoundUtils {

    private class RepeatingSound(
        val sound: SoundEvent,
        var remaining: Int,
        val volume: Float,
        val pitch: Float,
        val period: Int,
    ) {
        fun tick(time: Int) {
            if (time % period != 0) return
            remaining--
            McClient.playSound(sound, volume, pitch)
        }
    }

    private val repeatingSounds = mutableListOf<RepeatingSound>()

    fun playRepeated(sound: SoundEvent, amount: Int, volume: Float = 1f, pitch: Float = 1f, period: Int = 1) {
        repeatingSounds.add(RepeatingSound(sound, amount.coerceAtLeast(0), volume, pitch, period))
    }

    @Subscription
    fun onTick(event: TickEvent) {
        repeatingSounds.removeIf { sound ->
            sound.tick(event.ticks)
            sound.remaining <= 0
        }
    }

}
