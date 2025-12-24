package me.owdding.skyocean.api

import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.fishing.FishCatchEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.location.IslandChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.minecraft.sounds.SoundPlayedEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Module
object FishingCatchApi {

    val hook get() = McPlayer.self?.fishing

    var lastHookPos: Vec3? = null
        private set

    private val DELAY = 2.seconds
    private const val DISTANCE_SQR = 4

    private var lastPlingSound = Instant.DISTANT_PAST
    private var lastCatchSound = Instant.DISTANT_PAST

    @Subscription(TickEvent::class)
    @OnlyOnSkyBlock
    fun onTick() {
        lastHookPos = hook?.position() ?: return
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onSound(event: SoundPlayedEvent) {
        when (event.sound) {
            SoundEvents.PLAYER_SPLASH -> {
                if (event.volume != 0.25f) return
                val lastHook = lastHookPos ?: return
                if (lastHook.distanceToSqr(event.pos) > DISTANCE_SQR) return
                lastCatchSound = currentInstant()
                handleBobber()
            }
            SoundEvents.NOTE_BLOCK_PLING.value() -> {
                if (event.pitch != 1f || event.volume != 1f) return
                if (McPlayer.distanceSqr(event.pos) > DISTANCE_SQR) return
                lastPlingSound = currentInstant()
                handleBobber()
            }
        }
    }

    private fun handleBobber() {
        if (lastPlingSound.since() > DELAY || lastCatchSound.since() > DELAY) return
        FishCatchEvent(lastHookPos ?: return).post(SkyBlockAPI.eventBus)
        lastPlingSound = Instant.DISTANT_PAST
        lastCatchSound = Instant.DISTANT_PAST
    }

    @Subscription(IslandChangeEvent::class)
    fun onWorldChange() {
        lastHookPos = null
        lastPlingSound = Instant.DISTANT_PAST
        lastCatchSound = Instant.DISTANT_PAST
    }

}
