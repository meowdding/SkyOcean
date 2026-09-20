package me.owdding.skyocean.events

import net.minecraft.client.multiplayer.ClientLevel
import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent

data class ClientLevelChangeEvent(val clientLevel: ClientLevel?) : SkyBlockEvent()
