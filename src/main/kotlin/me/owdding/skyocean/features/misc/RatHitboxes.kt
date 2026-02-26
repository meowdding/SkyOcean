package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.MiscConfig
//? < 1.21.11{
/*import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.client.renderer.rendertype.RenderTypes
*///?}
import net.minecraft.client.gui.components.debug.DebugScreenEntries
//? > 1.21.10 {
import net.minecraft.gizmos.GizmoStyle
import net.minecraft.gizmos.Gizmos
//?}
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.zombie.Zombie
import net.minecraft.world.phys.AABB
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.math.abs

@Module
object RatHitboxes {
    private val rats = CopyOnWriteArraySet<Zombie>()

    @TimePassed("5t")
    @Subscription(TickEvent::class)
    fun tick() {
        rats.clear()
        if (!McLevel.hasLevel) return
        if (!MiscConfig.ratHitbox) return
        if (!SkyBlockIsland.HUB.inIsland()) return
        val player = McPlayer.self ?: return
        rats.addAll(
            McLevel.level.getEntities(
                player,
                AABB(player.blockPosition()).inflate(20.0),
            ).filterIsInstance<Zombie>()
                .filter { it.isBaby }
                .filter { abs(it.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) - 0.23000000417232513) <= 0.000001 }
                .toList(),
        )
    }

    private fun renderHitbox(): Boolean =  McClient.self.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)

    @Subscription
    @OnlyIn(SkyBlockIsland.HUB)
    fun entityRenderEvent(event: RenderWorldEvent.AfterTranslucent) {
        if (!renderHitbox()) return

        event.atCamera {
            if (!McLevel.hasLevel) return@atCamera
            rats.forEach {
                //? > 1.21.10 {
                Gizmos.cuboid(it.boundingBox, GizmoStyle.stroke(-1))
                //?} else {
                /*ShapeRenderer.renderLineBox(
                    event.poseStack.last(),
                    event.buffer.getBuffer(RenderTypes.LINES),
                    it.boundingBox,
                    1f, 1f, 1f, 1f,
                )
                *///?}
            }
        }
    }

}
