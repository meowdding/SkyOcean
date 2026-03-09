package me.owdding.skyocean.events

import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.debug.RegisterSkyOceanDebugEvent
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
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
    }
}
