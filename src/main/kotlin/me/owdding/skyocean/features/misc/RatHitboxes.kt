package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.MiscConfig
//? if > 1.21.8
import net.minecraft.client.gui.components.debug.DebugScreenEntries
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Zombie
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

    private fun renderHitbox(): Boolean {
        //? if > 1.21.8 {
        return McClient.self.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)
        //?} else {
        /*return McClient.self.entityRenderDispatcher.shouldRenderHitBoxes()
        *///?}
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.HUB)
    fun entityRenderEvent(event: RenderWorldEvent.AfterTranslucent) {
        if (!renderHitbox()) return

        event.atCamera {
            if (!McLevel.hasLevel) return@atCamera
            rats.forEach {
                ShapeRenderer.renderLineBox(
                    //? if > 1.21.8 {
                    event.poseStack.last(),
                    //?} else {
                    /*event.poseStack,
                    *///?}
                    event.buffer.getBuffer(RenderType.LINES),
                    it.boundingBox,
                    1f, 1f, 1f, 1f,
                )
            }
        }
    }

}
