package me.owdding.skyocean.utils

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

abstract class SkyOceanScreen(title: Component = CommonComponents.EMPTY) : BaseCursorScreen(title) {
    fun Layout.applyLayout() {
        this.visitWidgets {
            it.isFocused = true
            this@SkyOceanScreen.addRenderableWidget(it)
        }
    }

    fun Layout.center(): Layout = this.apply {
        FrameLayout.centerInRectangle(this, 0, 0, this@SkyOceanScreen.width, this@SkyOceanScreen.height)
        this.arrangeElements()
    }
}
