package me.owdding.skyocean.utils.extensions

import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.skyocean.accessors.ClearableLayout
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LinearLayout

fun Layout.tryClear() = apply {
    (this as? ClearableLayout)?.`skyocean$clear`()
}

fun <T : Layout> LayoutWidget<T>.clear() = apply { withContents { it.tryClear() } }

fun LayoutWidget<FrameLayout>.setFrameContent(content: LayoutElement) = apply { clear().withContents { it.addChild(content) } }
fun LayoutWidget<LinearLayout>.setLayoutContent(content: LayoutElement) = apply { clear().withContents { it.addChild(content) } }
