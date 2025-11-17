package me.owdding.skyocean.utils.tags

import me.owdding.skyocean.SkyOcean
import net.minecraft.resources.ResourceLocation

enum class SkyblockItemTagKey(val path: String) : BaseSkyblockItemTagKey {
    DRILLS("drills"),
    HUNT_AXES("hunt_axes"),
    ;

    override val location: ResourceLocation = SkyOcean.id(path)
    override val tag = load()
}
