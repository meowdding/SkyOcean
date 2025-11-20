package me.owdding.skyocean.events.fishing

import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent

data class FishCatchEvent(val hookPos: Vec3) : SkyBlockEvent()
