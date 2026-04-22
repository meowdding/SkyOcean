package me.owdding.skyocean.events

import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.debug.RegisterSkyOceanDebugEvent
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
//? >= 26.1
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents
import net.minecraft.client.multiplayer.ClientLevel
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI

@Module
object FmlApi {
    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, context ->
            RegisterSkyOceanCommandEvent(dispatcher, context).apply {
                post(SkyBlockAPI.eventBus)
                RegisterSkyOceanDebugEvent(this).post(SkyBlockAPI.eventBus)
            }
        }
        //? >= 26.1 {
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register { _, level: ClientLevel? ->
            ClientLevelChangeEvent(level).post(SkyBlockAPI.eventBus)
        }
        //? }
    }
}
