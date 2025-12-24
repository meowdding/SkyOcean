package me.owdding.skyocean.utils.tags

import me.owdding.skyocean.SkyOcean
import net.minecraft.resources.Identifier

enum class SkyblockItemTagKey(val path: String) : BaseSkyblockItemTagKey {
    DRILLS("drills"),
    HUNT_AXES("hunt_axes"),
    ;

    override val location: Identifier = SkyOcean.id(path)
    override val tag = load()
}
