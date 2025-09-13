package me.owdding.skyocean.utils.extensions

import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.skyocean.accessors.ClearableLayout
import net.minecraft.client.gui.layouts.Layout

fun Layout.tryClear() = apply {
    (this as? ClearableLayout)?.`skyocean$clear`()
}

fun <T : Layout> LayoutWidget<T>.clear() = apply { withContents { it.tryClear() } }
