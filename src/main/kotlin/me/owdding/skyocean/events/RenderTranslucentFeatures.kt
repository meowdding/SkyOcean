package me.owdding.skyocean.events

import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent

data class RenderTranslucentFeatures(
    val event: RenderWorldEvent.AfterTranslucent,
) : SkyBlockEvent()
