package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.skyocean.api.HotspotAPI
import me.owdding.skyocean.api.HotspotType
import me.owdding.skyocean.config.features.fishing.HotspotFeaturesConfig
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.events.fishing.HotspotEvent
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.extensions.distance
import me.owdding.skyocean.utils.extensions.horizontalDistance
import me.owdding.skyocean.utils.extensions.toBlockPos
import me.owdding.skyocean.utils.extensions.verticalDistance
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCircle
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCylinder
import net.minecraft.core.BlockPos
import net.minecraft.util.ARGB
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick
import kotlin.time.Duration.Companion.seconds

@Module
object HotspotFeatures {

    private const val MIN_DISTANCE = 40

    private val validLiquids = listOf<Fluid>(Fluids.WATER, Fluids.LAVA, Fluids.FLOWING_WATER, Fluids.FLOWING_LAVA)

    fun isEnabled() = HotspotFeaturesConfig.circleOutline || HotspotFeaturesConfig.circleSurface || HotspotFeaturesConfig.circleColumn

    fun shouldHideParticles() = isEnabled() && HotspotFeaturesConfig.hideParticles

    fun maxHotspotHeight(hotspotHeight: Float): Float = when (LocationAPI.island) {
        SkyBlockIsland.JERRYS_WORKSHOP -> 7f
        // here to handle that one part of bayou water that goes into a pipe because hotspots can spawn above that
        SkyBlockIsland.BACKWATER_BAYOU if (hotspotHeight > 85f) -> 7f
        // the lowest depth possible below that height is 4 but above that a depth of 4 does not happen and a cylinder with that height can be seen through lower parts of the terrain
        SkyBlockIsland.LOTUS_ATOLL if (hotspotHeight > 71f) -> 2f
        SkyBlockIsland.TORRHUS_CANYON if (hotspotHeight > 155f) -> 112f
        else -> 4f // most islands have a depth of 2, 3 or 4 blocks and don't seem to clip through the ceiling in my brief checks
    }

    fun yMatcher(y: Float): Float {
        if (!HotspotFeaturesConfig.circleMatchesPlayerY) return y
        val playerPos: Vec3 = McPlayer.position ?: return y
        val fluidAtPlayer = McLevel[playerPos.toBlockPos()].fluidState.type
        return if (playerPos.y <= y && (playerPos.y - y) >= maxHotspotHeight(y).unaryMinus() && validLiquids.contains(fluidAtPlayer)) playerPos.y.toFloat() + 0.01f
        else y
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onRenderWorldEvent(event: RenderWorldEvent.CollectSubmits) {
        if (!isEnabled()) return

        HotspotAPI.hotspots.forEach { (_, type, pos, radius) ->
            val radius = radius?.toFloat() ?: return@forEach
            val pos = pos ?: return@forEach

            if (HotspotFeaturesConfig.circleOutline) {
                event.renderCylinder(
                    pos.x, yMatcher(pos.y), pos.z,
                    radius,
                    0.1f,
                    ARGB.color(HotspotFeaturesConfig.outlineTransparency, type.color.value),
                )
            }

            if (HotspotFeaturesConfig.circleSurface) {
                event.renderCircle(
                    pos.x, yMatcher(pos.y), pos.z,
                    radius,
                    ARGB.color(HotspotFeaturesConfig.surfaceTransparency, type.color.value),
                )
            }

            if (HotspotFeaturesConfig.circleColumn) {
                val hotspotHeight = maxHotspotHeight(pos.y)
                event.renderCylinder(
                    pos.x, pos.y - hotspotHeight, pos.z,
                    radius,
                    hotspotHeight,
                    ARGB.color(HotspotFeaturesConfig.columnTransparency, type.color.value),
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

    @Subscription(TickEvent::class)
    @TimePassed("10t")
    @OnlyOnSkyBlock
    fun onTick() {
        if (HotspotFeaturesConfig.announce == HotspotFeaturesConfig.AnnouncementType.OFF) return

        val availableHotspots = HotspotAPI.hotspots
            .filter { !it.prompt.announced }
            .filter { it.pos != null && it.radius != null && it.type != HotspotType.UNKNOWN }
        if (availableHotspots.isEmpty()) return

        val playerPos = McPlayer.position ?: return
        val closest = availableHotspots.minByOrNull { it.pos?.distance(playerPos) ?: Float.MAX_VALUE } ?: return

        val hotspotPos = closest.pos ?: return
        val hotspotRadius = closest.radius ?: return

        if (hotspotPos.horizontalDistance(playerPos) > hotspotRadius) return
        if (hotspotPos.verticalDistance(playerPos) > 4.0) return

        val chatPos = hotspotPos.toBlockPos()
        when (HotspotFeaturesConfig.announce) {
            HotspotFeaturesConfig.AnnouncementType.MANUAL -> {
                if (closest.prompt.prompted || closest.prompt.announced) return

                Text.of {
                    append(ChatUtils.ICON_SPACE_COMPONENT)
                    append(
                        Text.of {
                            append("CLICK HERE")
                            color = CatppuccinColors.Frappe.peach
                        },
                    )
                    append(" to announce the ")
                    append(closest.type.displayComponent)
                    append(" Hotspot in chat.")

                    color = CatppuccinColors.Frappe.text

                    onClick {
                        closest.prompt.announced = true
                        HotspotFeaturesConfig.chatType.announce(hotspotMessage(closest.type, chatPos))
                    }
                }.send()
                closest.prompt.prompted = true
            }

            HotspotFeaturesConfig.AnnouncementType.AUTOMATIC -> {
                if (closest.prompt.prompted || closest.prompt.announced) return

                closest.prompt.announced = true
                HotspotFeaturesConfig.chatType.announce(hotspotMessage(closest.type, chatPos))
            }

            else -> return
        }
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("testHotspot") {
            callback {
                HotspotType.entries.random().let { type ->
                    val chatPos = McPlayer.position?.toBlockPos() ?: return@callback
                    McClient.connection?.sendChat(hotspotMessage(type, chatPos))
                }
                Text.of(ChatUtils.antiSpam()).send()
            }
        }
    }

    private fun hotspotMessage(type: HotspotType, pos: BlockPos): String = buildString {
        append("x: ${pos.x}, y: ${pos.y}, z: ${pos.z} | ")
        append(type.announcementName)
        append(" | ")
        append(ChatUtils.antiSpam())
    }
}
