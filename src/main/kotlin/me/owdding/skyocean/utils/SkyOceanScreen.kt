package me.owdding.skyocean.utils

import me.owdding.lib.platform.screens.MeowddingScreen
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.Text


abstract class SkyOceanScreen(title: Component = CommonComponents.EMPTY) : MeowddingScreen(title) {
    constructor(title: String) : this(Text.of(title))

    fun olympus(path: String) = SkyOcean.olympus(path)

    fun LayoutElement.applyLayout() {
        this.visitWidgets {
            it.isFocused = true
            this@SkyOceanScreen.addRenderableWidget(it)
        }
    }

    fun LayoutElement.center() = this.apply {
        FrameLayout.centerInRectangle(this, 0, 0, this@SkyOceanScreen.width, this@SkyOceanScreen.height)
        if (this is Layout) {
            this.arrangeElements()
        }
    }

    fun LayoutElement.applyAndGetElements(): MutableList<AbstractWidget> {
        this.applyLayout()
        val elements = mutableListOf<AbstractWidget>()
        this.visitWidgets(elements::add)
        return elements
    }

    fun LayoutElement.applyAsRenderable() {
        this.visitWidgets {
            it.isFocused = true
            this@SkyOceanScreen.addRenderableOnly(it)
        }
    }

}
