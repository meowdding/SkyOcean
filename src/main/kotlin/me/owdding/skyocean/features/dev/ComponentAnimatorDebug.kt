package me.owdding.skyocean.features.dev

import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.chat.ComponentAnimator
import me.owdding.skyocean.utils.debugToggle
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.render.RenderHudEvent
import tech.thatgravyboat.skyblockapi.helpers.McFont

@Module
object ComponentAnimatorDebug {

    private val toggle by debugToggle("component_animator")

    private val component by ComponentAnimator("Component Animator Debug", 0xFF0000, 0x00FF00)

    @Subscription
    fun onRender(event: RenderHudEvent) {
        if (!toggle) return
        event.graphics.drawString(
            McFont.self,
            component,
            10,
            10,
            0xFFFFFFFF.toInt(),
            false,
        )
    }

}
