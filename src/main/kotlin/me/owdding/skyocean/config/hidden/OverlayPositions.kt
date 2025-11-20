package me.owdding.skyocean.config.hidden

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.lib.overlays.ConfigPosition

object OverlayPositions : CategoryKt("overlays") {
    override val hidden: Boolean = true

    val craftHelper by obj(ConfigPosition(5, 200))
}
