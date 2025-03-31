package codes.cookies.skyocean.events

import codes.cookies.skyocean.modules.Module
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI

@Module
object FmlApi {
    init {
        WorldRenderEvents.AFTER_TRANSLUCENT.register { ctx ->
            RenderWorldEvent(ctx.matrixStack()!!, ctx.consumers()!!, ctx.camera()).post(SkyBlockAPI.eventBus)
        }
    }
}
