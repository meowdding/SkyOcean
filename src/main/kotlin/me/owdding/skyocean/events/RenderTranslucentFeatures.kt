package me.owdding.skyocean.events

import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent

//? <= 26.1
//import net.minecraft.client.renderer.MultiBufferSource

data class RenderTranslucentFeatures(
    val event: RenderWorldEvent.AfterTranslucent
) : SkyBlockEvent()
