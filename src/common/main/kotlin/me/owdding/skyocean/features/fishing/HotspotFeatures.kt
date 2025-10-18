package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.skyocean.api.HotspotAPI
import me.owdding.skyocean.config.features.fishing.HotspotFeaturesConfig
import me.owdding.skyocean.events.fishing.HotspotEvent
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCircle
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCylinder
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration.Companion.seconds

@Module
object HotspotFeatures {

    private const val MIN_DISTANCE = 40

    fun isEnabled() = HotspotFeaturesConfig.circleOutline || HotspotFeaturesConfig.circleSurface

    @Subscription
    @OnlyOnSkyBlock
    fun onRenderWorldEvent(event: RenderWorldEvent.AfterTranslucent) {
        if (!isEnabled()) return

        HotspotAPI.hotspots.forEach { (_, type, pos, radius) ->
            val radius = radius ?: return@forEach
            val pos = pos ?: return@forEach

            if (HotspotFeaturesConfig.circleOutline) {
                event.renderCylinder(
                    pos.x, pos.y, pos.z,
                    radius.toFloat(),
                    0.1f,
                    ARGB.color(HotspotFeaturesConfig.outlineTransparency, type.color.value),
                )
            }

            if (HotspotFeaturesConfig.circleSurface) {
                event.renderCircle(
                    pos.x, pos.y, pos.z,
                    radius.toFloat(),
                    ARGB.color(HotspotFeaturesConfig.surfaceTransparency, type.color.value),
                )
            }
        }
    }

    @Subscription
    fun onHotspotDespawn(event: HotspotEvent.Despawn) {
        if (!HotspotFeaturesConfig.warning) return
        val hotspot = event.hotspot
        if (!hotspot.fishedIn || HotspotAPI.lastHotspotFish.since() > 30.seconds) return
        val playerPos = McPlayer.position ?: return
        val distance = hotspot.pos?.distance(playerPos.toVector3f()) ?: return
        if (distance > MIN_DISTANCE) return
        text {
            append(hotspot.type.displayComponent)
            append(" Hotspot despawned!", OceanColors.WARNING)
        }.sendWithPrefix()
        McClient.setTitle(
            text {
                append(ChatUtils.ICON_SPACE_COMPONENT)
                append("Hotspot despawned!", OceanColors.WARNING)
            },
            stayTime = 3f,
            fadeOutTime = 0.5f,
        )
    }

}
