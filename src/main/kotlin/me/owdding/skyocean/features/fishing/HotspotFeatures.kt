package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.floor
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
import me.owdding.skyocean.utils.extensions.toBlockPos
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCircle
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCylinder
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.core.BlockPos
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

@Module
object HotspotFeatures {

    private const val MIN_DISTANCE = 40

    init {
        LevelRenderEvents.COLLECT_SUBMITS.register {
            if (!LocationAPI.isOnSkyBlock) return@register
            onRenderWorldEvent(RenderWorldEvent.AfterEntities(
                it.poseStack(),
                //? 26.1
                //it.bufferSource(),
                it.submitNodeCollector(),
                //~ if >= 26.2 '.mainCamera.' -> '.mainCamera().' {
                it.gameRenderer().mainCamera().position(),
                it.gameRenderer().mainCamera().rotation(),
                //~}
                0f,
            ))
        }
    }

    fun isEnabled() = HotspotFeaturesConfig.circleOutline || HotspotFeaturesConfig.circleSurface

    fun shouldHideParticles() = isEnabled() && HotspotFeaturesConfig.hideParticles

    //Subscription
    //OnlyOnSkyBlock
    fun onRenderWorldEvent(event: RenderWorldEvent.AfterEntities) {
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
                    append(Text.of {
                        append("CLICK HERE")
                        color = CatppuccinColors.Frappe.peach
                        onClick {
                            closest.prompt.announced = true
                            McClient.connection?.sendChat(
                                "${closest.type.annoucementName} Hotspot at " +
                                    "x: ${chatPos.x}, y: ${chatPos.y}, z: ${chatPos.z} | " +
                                    ChatUtils.antiSpam()
                            )
                        }
                    })
                    append(CommonText.SPACE)
                    append("to announce the ${closest.type.annoucementName} Hotspot in chat.")
                    color = CatppuccinColors.Frappe.text
                }.send()
                closest.prompt.prompted = true
            }
            HotspotFeaturesConfig.AnnouncementType.AUTOMATIC -> {
                if (closest.prompt.prompted || closest.prompt.announced) return

                McClient.connection?.sendChat(
                    "${closest.type.annoucementName} Hotspot at " +
                        "x: ${chatPos.x}, y: ${chatPos.y}, z: ${chatPos.z} | " +
                        ChatUtils.antiSpam()
                )
                closest.prompt.announced = true
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
                    McClient.connection?.sendChat(
                        "${type.annoucementName} Hotspot at " +
                            "x: ${chatPos.x}, y: ${chatPos.y}, z: ${chatPos.z} | " +
                            ChatUtils.antiSpam()
                    )
                }
                Text.of(ChatUtils.antiSpam()).send()
            }
        }
    }

    private fun Vector3f.distance(other: Vec3) = distance(other.x.toFloat(), other.y.toFloat(), other.z.toFloat())

    private fun Vector3f.horizontalDistance(other: Vec3): Float {
        val dx = this.x - other.x.toFloat()
        val dz = this.z - other.z.toFloat()
        return sqrt(dx * dx + dz * dz)
    }

    private fun Vector3f.verticalDistance(other: Vec3): Float = abs(this.y - other.y.toFloat())

    private fun Vector3f.toBlockPos() = BlockPos(x.floor(), y.floor(), z.floor())
}
