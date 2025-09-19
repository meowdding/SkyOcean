package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.lib.utils.RenderUtils.renderTextInWorld
import me.owdding.skyocean.config.features.fishing.FishingConfig
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@Module
object BobberTime {

    @Subscription
    @OnlyOnSkyBlock
    fun onRender(event: RenderWorldEvent.AfterTranslucent) {
        if (!FishingConfig.bobberTime) return
        val rod = McPlayer.self?.fishing ?: return
        val time = rod.tickCount / 20.0

        val bobberTextScaleOffset = McFont.height * FishingConfig.hookTextScale

        event.renderTextInWorld(
            rod.position().add(-0.5, -0.3, -0.5),
            Text.of("${"%.2f".format(time)}s").withColor(TextColor.GRAY),
            center = true,
            yOffset = -bobberTextScaleOffset,
        )
    }

}
