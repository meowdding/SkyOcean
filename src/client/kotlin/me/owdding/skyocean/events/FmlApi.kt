package me.owdding.skyocean.events

import me.owdding.ktmodules.Module
import me.owdding.skyocean.generated.SkyOceanRepoModules
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI

@Module
object FmlApi {
    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            SkyOceanRepoModules.init { SkyBlockAPI.eventBus.register(it) }
            RegisterSkyOceanCommandEvent(dispatcher).post(SkyBlockAPI.eventBus)
        }
    }
}
