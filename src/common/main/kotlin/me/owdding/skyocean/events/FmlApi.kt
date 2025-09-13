package me.owdding.skyocean.events

import me.owdding.ktmodules.Module
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI

@Module
object FmlApi {
    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, context ->
            RegisterSkyOceanCommandEvent(dispatcher, context).post(SkyBlockAPI.eventBus)
        }
    }
}
