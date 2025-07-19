package me.owdding.skyocean.features.foraging.galatea

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.foraging.GalateaConfig
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.minecraft.sounds.SoundPlayedEvent

@Module
object MuteTheFuckingPhantoms {

    val phantom = setOf(
        SoundEvents.PHANTOM_AMBIENT,
        SoundEvents.PHANTOM_BITE,
        SoundEvents.PHANTOM_DEATH,
        SoundEvents.PHANTOM_FLAP,
        SoundEvents.PHANTOM_HURT,
        SoundEvents.PHANTOM_SWOOP,
    )

    @Subscription
    @OnlyOnSkyBlock
    fun onSound(event: SoundPlayedEvent) {
        if (!GalateaConfig.muteThePhantoms) return

        if (event.sound in phantom) {
            event.cancel()
        }
    }

}
