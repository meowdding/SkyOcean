package me.owdding.skyocean.events.fishing

import me.owdding.skyocean.api.WormholeData
import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent

sealed class WormholeEvent(val wormhole: WormholeData) : SkyBlockEvent() {
    class Spawn(wormhole: WormholeData) : WormholeEvent(wormhole)
    class Despawn(wormhole: WormholeData) : WormholeEvent(wormhole)
}
