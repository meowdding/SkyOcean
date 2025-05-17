package me.owdding.skyocean.features.fishing

import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RenderWorldEvent
import me.owdding.skyocean.utils.rendering.RenderUtils
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityRemovedEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.NameChangedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object HotspotHighlighter {

    private val hotspots = mutableMapOf<Int, Vec3>()

    @Subscription
    fun onNameChanged(event: NameChangedEvent) {
        if (event.component.stripped != "HOTSPOT") return
        hotspots[event.infoLineEntity.id] = event.infoLineEntity.position()
    }

    @Subscription(event = [ServerChangeEvent::class])
    fun onServerChange() {
        hotspots.clear()
    }

    @Subscription
    fun onEntityRemoved(event: EntityRemovedEvent) {
        if (event.entity.id !in hotspots) return
        hotspots.remove(event.entity.id)
    }

    @Subscription
    fun onRenderWorldEvent(event: RenderWorldEvent) {
        // TODO add config
        this.hotspots.forEach { (_, pos) ->
            RenderUtils.renderCricle(
                event,
                pos.x.toFloat(),
                pos.y.toFloat() - 1.5f,
                pos.z.toFloat(),
                3f, // TODO get radius by the type
                0.5f,
                MinecraftColors.LIGHT_PURPLE.withAlpha(100).value // TODO color by the type
            )
        }
    }
}
