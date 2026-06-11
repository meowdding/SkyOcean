package me.owdding.skyocean.features.foraging.galatea

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.foraging.GalateaConfig
import me.owdding.skyocean.config.features.misc.MiscConfig
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.player.Player
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityAttributesUpdateEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityRemovedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.minecraft.sounds.SoundPlayedEvent
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getLeggings
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import java.util.*

@Module
object MuteTheFuckingStereoPants {

    private val sounds = setOf(
        SoundEvents.NOTE_BLOCK_HARP.value(),
        SoundEvents.NOTE_BLOCK_BASS.value(),
        SoundEvents.NOTE_BLOCK_SNARE.value(),
        SoundEvents.NOTE_BLOCK_HAT.value(),
        SoundEvents.NOTE_BLOCK_BASEDRUM.value(),
        SoundEvents.GLASS_PLACE,
    )

    private val players = WeakHashMap<Player, Boolean>()

    @Subscription
    @OnlyOnSkyBlock
    fun onEntity(event: EntityAttributesUpdateEvent) {
        val player = event.entity as? Player ?: return
        if (players.containsKey(player)) return
        if (player.getLeggings().getSkyBlockId()?.equals("MUSIC_PANTS", true) == true) {
            players[player] = true
        }
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onSound(event: SoundPlayedEvent) {
        if (!MiscConfig.stereoPantsMute) return
        val shouldHide = players.keys.any { it.distanceToSqr(event.pos) < 3.0 } && event.sound in sounds

        if (shouldHide) {
            event.cancel()
        }
    }


    @Subscription
    fun onEntityLeave(event: EntityRemovedEvent) {
        val player = event.entity as? AbstractClientPlayer ?: return
        players.remove(player)
    }

    @Subscription
    fun onWorldSwitch(event: ServerChangeEvent) {
        players.clear()
    }
}
