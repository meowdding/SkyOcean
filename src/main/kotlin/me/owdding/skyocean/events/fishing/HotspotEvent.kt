package me.owdding.skyocean.events.fishing

import me.owdding.skyocean.api.HotspotData
import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent

sealed class HotspotEvent(val hotspot: HotspotData) : SkyBlockEvent() {
    class Spawn(hotspot: HotspotData) : HotspotEvent(hotspot)
    class Despawn(hotspot: HotspotData) : HotspotEvent(hotspot)
}
