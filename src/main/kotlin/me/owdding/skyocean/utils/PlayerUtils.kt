package me.owdding.skyocean.utils

import me.owdding.ktmodules.Module
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

@Module
object PlayerUtils {

    var lastPos: Vec3 = Vec3.ZERO
        private set
    var lastMoveTime: Long = 0

    @Subscription
    fun onTick(event: TickEvent) {
        val currentPos = McPlayer.position ?: Vec3.ZERO
        if (currentPos != lastPos) {
            lastPos = currentPos
            lastMoveTime = System.currentTimeMillis()
        }
    }

}
