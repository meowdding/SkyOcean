package me.owdding.skyocean.features.fishing

import com.teamresourceful.resourcefullib.common.color.Color
import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.fishing.HotspotHighlightConfig
import me.owdding.skyocean.events.fishing.FishCatchEvent
import me.owdding.skyocean.utils.Utils.roundToHalf
import me.owdding.skyocean.utils.extensions.toBlockPos
import me.owdding.skyocean.utils.extensions.toVector3d
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCircle
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCylinder
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3
import org.intellij.lang.annotations.Language
import org.joml.Vector2d
import org.joml.Vector3d
import org.joml.Vector3f
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityRemovedEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.NameChangedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.PacketReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.utils.extentions.forEachBelow
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Module
object HotspotHighlighter {

    private val PARTICLE_COLOR = Vector3f(1.0f, 0.4117647f, 0.7058824f)

    private val hotspots = mutableMapOf<Vector2d, HotspotData>()
    private var lastHotspotFish = Instant.DISTANT_PAST

    private fun isEnabled() = HotspotHighlightConfig.circleOutline || HotspotHighlightConfig.circleSurface

    @Subscription
    fun onNameChanged(event: NameChangedEvent) {
        val pos = event.infoLineEntity.position()
        val type = HotspotType.getType(event.literalComponent) ?: return
        val hotspot = hotspots.getOrPut(pos.toVec2d()) {
            HotspotData(id = event.infoLineEntity.id, type = type)
        }

        BlockPos.containing(pos).forEachBelow(3) {
            val fluid = McLevel[it].fluidState

            if (!fluid.isEmpty) {
                hotspot.pos = Vector3d(pos.x, it.y.toDouble() + fluid.getHeight(McLevel.self, it), pos.z)
                return
            }
        }
    }

    @Subscription
    fun onCatch(event: FishCatchEvent) {
        val hookY = event.hookPos.y
        val hookPosD = event.hookPos.toVector3d()
        hotspots.values.filter {
            val y = it.pos?.y ?: return@filter false
            abs(hookY - y) < 3
        }.minByOrNull { it.pos?.distanceSquared(hookPosD) ?: Double.MAX_VALUE }?.let {
            it.fishedIn = true
            lastHotspotFish = currentInstant()
        }
    }

    @Subscription(event = [ServerChangeEvent::class])
    fun onServerChange() {
        hotspots.clear()
    }

    @Subscription
    fun onEntityRemoved(event: EntityRemovedEvent) {
        val pos = event.entity.position().toVec2d()
        val hotspot = hotspots[pos] ?: return
        if (hotspot.id == event.entity.id) {
            if (hotspot.fishedIn && lastHotspotFish.since() < 30.seconds) {
                // warn
            }
            hotspots.remove(pos)
        }
    }

    @Subscription
    fun onParticle(event: PacketReceivedEvent) {
        if (!this.isEnabled()) return

        val packet = event.packet as? ClientboundLevelParticlesPacket ?: return
        if (!packet.isHotSpotParticle()) return

        val maxHotspotSize = when (LocationAPI.island) {
            SkyBlockIsland.CRIMSON_ISLE -> 25.0
            else -> 9.0
        }

        for (entry in hotspots.values) {
            if (entry.pos == null) continue

            val distance = ((packet.x - entry.pos!!.x).pow(2) + (packet.z - entry.pos!!.z).pow(2))
            if (distance <= maxHotspotSize + 0.5) {
                entry.radius = sqrt(distance).roundToHalf()
                event.cancel()
                return
            }
        }
    }

    @Subscription
    fun onRenderWorldEvent(event: RenderWorldEvent.AfterTranslucent) {
        if (!this.isEnabled()) return

        this.hotspots.values.forEach { (_, type, pos, radius) ->
            val radius = radius ?: return@forEach
            val pos = pos ?: return@forEach

            if (HotspotHighlightConfig.circleOutline) {
                event.renderCylinder(
                    pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(),
                    radius.toFloat(),
                    0.1f,
                    ARGB.color(HotspotHighlightConfig.outlineTransparency, type.color.value),
                )
            }

            if (HotspotHighlightConfig.circleSurface) {
                event.renderCircle(
                    pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(),
                    radius.toFloat(),
                    ARGB.color(HotspotHighlightConfig.surfaceTransparency, type.color.value),
                )
            }
        }
    }

    private fun ClientboundLevelParticlesPacket.isHotSpotParticle(): Boolean {
        if (LocationAPI.island == SkyBlockIsland.CRIMSON_ISLE) {
            return this.particle.type == ParticleTypes.SMOKE && (this.count == 5 || this.count == 2)
        }
        val options = this.particle as? DustParticleOptions ?: return false
        return options.color == PARTICLE_COLOR
    }

    private fun Vec3.toVec2d(): Vector2d = Vector2d(this.x, this.z)
}

data class HotspotData(
    val id: Int,
    val type: HotspotType,
    var pos: Vector3d? = null,
    var radius: Double? = null,
    var fishedIn: Boolean = false,
)

enum class HotspotType(val color: Color, @Language("regexp") regex: String) {
    SEA_CREATURE(MinecraftColors.DARK_AQUA, "\\+\\d+α Sea Creature Chance"),
    FISHING_SPEED(MinecraftColors.AQUA, "\\+\\d+☂ Fishing Speed"),
    DOUBLE_HOOK(MinecraftColors.BLUE, "\\+\\d+⚓ Double Hook Chance"),
    TREASURE(MinecraftColors.GOLD, "\\+\\d+⛃ Treasure Chance"),
    TROPHY_FISH(MinecraftColors.GOLD, "\\+\\d+♔ Trophy Fish Chance"),
    UNKNOWN(MinecraftColors.LIGHT_PURPLE, ""),
    ;

    val regex: Regex = Regex(regex)

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
