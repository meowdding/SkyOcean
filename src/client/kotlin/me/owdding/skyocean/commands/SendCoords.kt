package me.owdding.skyocean.commands

import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.component1
import tech.thatgravyboat.skyblockapi.utils.extentions.component2
import tech.thatgravyboat.skyblockapi.utils.extentions.component3

@Module
object SendCoords {

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("sendcoords") {
            val (x, y, z) = McPlayer.self?.blockPosition() ?: return@registerWithCallback
            McClient.self.player?.connection?.sendChat("x: $x, y: $y, z: $z")
        }
    }
}
