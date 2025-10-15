package me.owdding.skyocean.utils.rendering

import me.owdding.ktmodules.AutoCollect
import me.owdding.ktmodules.Module
import me.owdding.lib.events.overlay.FinishOverlayEditingEvent
import me.owdding.lib.overlays.Overlay
import me.owdding.lib.overlays.Overlays
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.SkyOceanOverlays
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription

abstract class SkyOceanOverlay : Overlay {
    override val modId: String get() = SkyOcean.MOD_ID
}

@Module
object OceanOverlays {
    init {
        SkyOceanOverlays.collected.forEach { Overlays.register(it) }
    }

    @Subscription
    fun finishEditing(event: FinishOverlayEditingEvent) {
        if (event.modId == SkyOcean.MOD_ID) {
            SkyOcean.config.save()
        }
    }
}

@AutoCollect("Overlays")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Overlay
