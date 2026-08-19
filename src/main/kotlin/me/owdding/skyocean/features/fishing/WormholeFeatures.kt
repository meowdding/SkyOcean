package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.skyocean.api.WormholeAPI
import me.owdding.skyocean.config.features.fishing.WormholeFeaturesConfig
import me.owdding.skyocean.events.fishing.WormholeEvent
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCircle
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCylinder
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import kotlin.time.Duration.Companion.seconds

@Module
object WormholeFeatures {

    private const val MIN_DISTANCE = 40

    init {
        LevelRenderEvents.COLLECT_SUBMITS.register {
            if (!LocationAPI.isOnSkyBlock) return@register
            onRenderWorldEvent(
                RenderWorldEvent.AfterEntities(
                    it.poseStack(),
                    //? 26.1
                    //it.bufferSource(),
                    it.submitNodeCollector(),
                    //~ if >= 26.2 '.mainCamera.' -> '.mainCamera().' {
                    it.gameRenderer().mainCamera().position(),
                    it.gameRenderer().mainCamera().rotation(),
                    //~}
                    0f,
                ),
            )
        }
    }

    fun isEnabled() = WormholeAPI.inWormholeIsland() && (WormholeFeaturesConfig.circleOutline || WormholeFeaturesConfig.circleSurface)

    fun shouldHideParticles() = isEnabled() && WormholeFeaturesConfig.hideParticles

    //Subscription
    //OnlyOnSkyBlock
    fun onRenderWorldEvent(event: RenderWorldEvent.AfterEntities) {
        if (!isEnabled()) return

        WormholeAPI.wormholes.forEach { (pos, radius) ->
            val radius = radius ?: return@forEach
            val pos = pos ?: return@forEach

            if (WormholeFeaturesConfig.circleOutline) {
                event.renderCylinder(
                    pos.x, pos.y, pos.z,
                    radius.toFloat(),
                    0.1f,
                    ARGB.color(WormholeFeaturesConfig.outlineTransparency, WormholeFeaturesConfig.color),
                )
            }

            if (WormholeFeaturesConfig.circleSurface) {
                event.renderCircle(
                    pos.x, pos.y, pos.z,
                    radius.toFloat(),
                    ARGB.color(WormholeFeaturesConfig.surfaceTransparency, WormholeFeaturesConfig.color),
                )
            }
        }
    }

    @Subscription
    fun onWormholeDespawn(event: WormholeEvent.Despawn) {
        if (!WormholeFeaturesConfig.warning) return
        val wormhole = event.wormhole
        if (!wormhole.fishedIn || WormholeAPI.lastWormholeFish.since() > 30.seconds) return
        val playerPos = McPlayer.position ?: return
        val distance = wormhole.pos?.distance(playerPos.toVector3f()) ?: return
        if (distance > MIN_DISTANCE) return
        text {
            append("Wormhole despawned!", OceanColors.WARNING)
        }.sendWithPrefix()
        McClient.setTitle(
            text {
                append(ChatUtils.ICON_SPACE_COMPONENT)
                append("Wormhole despawned!", OceanColors.WARNING)
            },
            stayTime = 3f,
            fadeOutTime = 0.5f,
        )
    }

}
