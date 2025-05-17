package me.owdding.skyocean.events

import me.owdding.ktmodules.Module
import me.owdding.skyocean.generated.SkyOceanRepoModules
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI

@Module
object FmlApi {
    init {
        WorldRenderEvents.AFTER_TRANSLUCENT.register { ctx ->
            RenderWorldEvent(ctx.matrixStack()!!, ctx.consumers()!!, ctx.camera()).post(SkyBlockAPI.eventBus)
        }
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            SkyOceanRepoModules.init { SkyBlockAPI.eventBus.register(it) }
            RegisterSkyOceanCommandEvent(dispatcher).post(SkyBlockAPI.eventBus)
        }
    }
}
