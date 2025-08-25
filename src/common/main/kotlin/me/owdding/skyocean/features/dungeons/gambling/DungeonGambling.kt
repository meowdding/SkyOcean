package me.owdding.skyocean.features.dungeons.gambling

import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient

@Module
object DungeonGambling {

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("meowing") {
            McClient.setScreenAsync { DungeonGamblingScreen() }
        }
    }
}
