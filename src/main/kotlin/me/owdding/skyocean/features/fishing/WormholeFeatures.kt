package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.skyocean.api.WormholeAPI
import me.owdding.skyocean.config.features.fishing.WormholeFeaturesConfig
import me.owdding.skyocean.events.fishing.WormholeEvent
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.extensions.toBlockPos
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCircle
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCylinder
import net.minecraft.util.ARGB
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import kotlin.time.Duration.Companion.seconds

@Module
object WormholeFeatures {

    private const val MIN_DISTANCE = 40

    private val validLiquids = listOf<Fluid>(Fluids.WATER, Fluids.LAVA, Fluids.FLOWING_WATER, Fluids.FLOWING_LAVA)

    fun isEnabled() =
        WormholeAPI.inWormholeIsland() && (WormholeFeaturesConfig.circleOutline || WormholeFeaturesConfig.circleSurface || WormholeFeaturesConfig.circleColumn)

    fun shouldHideParticles() = isEnabled() && WormholeFeaturesConfig.hideParticles

    fun maxWormholeHeight(height: Float): Float = when (LocationAPI.island) {
        // the lowest depth below that height is potentially 4 but above that a depth of 4 does not happen and a cylinder with that height can be seen through lower parts of the terrain
        SkyBlockIsland.LOTUS_ATOLL if (height > 71f) -> 2f
        else -> 4f
    }

    fun yMatcher(y: Float): Float {
        if (!WormholeFeaturesConfig.circleMatchesPlayerY) return y
        val playerPos: Vec3 = McPlayer.position ?: return y
        val fluidAtPlayer = McLevel[playerPos.toBlockPos()].fluidState.type
        return if (playerPos.y <= y && (playerPos.y - y) >= maxWormholeHeight(y).unaryMinus() && validLiquids.contains(fluidAtPlayer)) playerPos.y.toFloat() + 0.01f
        else y
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onRenderWorldEvent(event: RenderWorldEvent.CollectSubmits) {
        if (!isEnabled()) return

        WormholeAPI.wormholes.forEach { (pos, radius) ->
            val radius = radius?.toFloat() ?: return@forEach
            val pos = pos ?: return@forEach

            if (WormholeFeaturesConfig.circleOutline) {
                event.renderCylinder(
                    pos.x, yMatcher(pos.y), pos.z,
                    radius,
                    0.1f,
                    ARGB.color(WormholeFeaturesConfig.outlineTransparency, WormholeFeaturesConfig.color),
                )
            }

            if (WormholeFeaturesConfig.circleSurface) {
                event.renderCircle(
                    pos.x, yMatcher(pos.y), pos.z,
                    radius,
                    ARGB.color(WormholeFeaturesConfig.surfaceTransparency, WormholeFeaturesConfig.color),
                )
            }

            if (WormholeFeaturesConfig.circleColumn) {
                val cylinderHeight = maxWormholeHeight(pos.y)
                event.renderCylinder(
                    pos.x, pos.y - cylinderHeight, pos.z,
                    radius,
                    cylinderHeight,
                    ARGB.color(WormholeFeaturesConfig.columnTransparency, WormholeFeaturesConfig.color),
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
