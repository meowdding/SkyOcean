package me.owdding.skyocean.features.fishing

import com.teamresourceful.resourcefullib.common.color.Color
import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.fishing.HotspotHighlightConfig
import me.owdding.skyocean.events.RenderWorldEvent
import me.owdding.skyocean.features.fishing.HotspotType.entries
import me.owdding.skyocean.utils.Utils.roundToHalf
import me.owdding.skyocean.utils.rendering.RenderUtils
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3
import org.intellij.lang.annotations.Language
import org.joml.Vector2d
import org.joml.Vector3f
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityRemovedEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.NameChangedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.PacketReceivedEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import kotlin.math.pow
import kotlin.math.sqrt

@Module
object HotspotHighlighter {

    private val PARTICLE_COLOR = Vector3f(1.0f, 0.4117647f, 0.7058824f)

    private val hotspots = mutableMapOf<Int, Vec3>()
    private val hotspotsTypes = mutableMapOf<Vector2d, HotspotType>()
    private val hotspotRadius = mutableMapOf<Int, Double>()

    private fun isEnabled() = HotspotHighlightConfig.circleOutline || HotspotHighlightConfig.circleSurface

    @Subscription
    fun onNameChanged(event: NameChangedEvent) {
        val pos = event.infoLineEntity.position()
        if (event.literalComponent == "HOTSPOT") {
            hotspots[event.infoLineEntity.id] = pos
        } else {
            HotspotType.getType(event.literalComponent)?.let { type ->
                hotspotsTypes[pos.toVec2d()] = type
            }
        }
    }

    @Subscription(event = [ServerChangeEvent::class])
    fun onServerChange() {
        hotspots.clear()
        hotspotRadius.clear()
        hotspotsTypes.clear()
    }

    @Subscription
    fun onEntityRemoved(event: EntityRemovedEvent) {
        if (event.entity.id !in hotspots) return
        hotspots.remove(event.entity.id)
        hotspotRadius.remove(event.entity.id)
        hotspotsTypes.remove(event.entity.position().toVec2d())
    }

    @Subscription
    fun onParticle(event: PacketReceivedEvent) {
        if (!this.isEnabled()) return

        val packet = event.packet as? ClientboundLevelParticlesPacket ?: return
        if (!packet.isHotSpotParticle()) return

        for ((id, pos) in hotspots) {
            val distance = ((packet.x - pos.x).pow(2) + (packet.z - pos.z).pow(2))
            if (distance <= 9.5) {
                McClient.tell {
                    hotspotRadius[id] = sqrt(distance).roundToHalf()
                }
                event.cancel()
                return
            }
        }
    }

    @Subscription
    fun onRenderWorldEvent(event: RenderWorldEvent) {
        if (!this.isEnabled()) return

        this.hotspots.forEach { (id, pos) ->
            val radius = hotspotRadius[id] ?: return@forEach
            val type = hotspotsTypes[pos.toVec2d()] ?: HotspotType.UNKNOWN

            if (HotspotHighlightConfig.circleOutline) {
                RenderUtils.renderCylinder(
                    event,
                    pos.x.toFloat(), pos.y.toFloat() - 1.5f, pos.z.toFloat(),
                    radius.toFloat(),
                    0.1f,
                    ARGB.color(HotspotHighlightConfig.outlineTransparency, type.color.value),
                )
            }

            if (HotspotHighlightConfig.circleSurface) {
                RenderUtils.renderCircle(
                    event,
                    pos.x.toFloat(), pos.y.toFloat() - 1.5f, pos.z.toFloat(),
                    radius.toFloat(),
                    ARGB.color(HotspotHighlightConfig.surfaceTransparency, type.color.value),
                )
            }
        }
    }

    private fun ClientboundLevelParticlesPacket.isHotSpotParticle(): Boolean {
        if (LocationAPI.island == SkyBlockIsland.CRIMSON_ISLE) {
            return this.particle.type == ParticleTypes.SMOKE && this.count == 5
        }
        val options = this.particle as? DustParticleOptions ?: return false
        return options.color == PARTICLE_COLOR
    }

    private fun Vec3.toVec2d(): Vector2d = Vector2d(this.x, this.z)
}

enum class HotspotType(val color: Color, val regex: Regex) {
    SEA_CREATURE(MinecraftColors.DARK_AQUA, "\\+\\d+α Sea Creature Chance"),
    FISHING_SPEED(MinecraftColors.AQUA, "\\+\\d+☂ Fishing Speed"),
    DOUBLE_HOOK(MinecraftColors.BLUE, "\\+\\d+⚓ Double Hook Chance"),
    TREASURE(MinecraftColors.GOLD, "\\+\\d+⛃ Treasure Chance"),
    TROPHY_FISH(MinecraftColors.GOLD, "\\+\\d+♔ Trophy Fish Chance"),
    UNKNOWN(MinecraftColors.LIGHT_PURPLE, ""),
    ;

    constructor(color: Color, @Language("regexp") regex: String): this(color, Regex(regex))

    companion object {

        fun getType(input: String): HotspotType? {
            for (type in entries) {
                if (type == UNKNOWN) continue
                if (type.regex.matches(input)) return type
            }
            return null
        }
    }
}
