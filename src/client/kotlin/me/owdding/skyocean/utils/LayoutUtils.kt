package me.owdding.skyocean.utils

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import com.teamresourceful.resourcefullib.common.utils.TriState
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.lib.builder.LayoutFactory
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

abstract class SkyOceanScreen(title: Component = CommonComponents.EMPTY) : BaseCursorScreen(title) {
    fun LayoutElement.applyLayout() {
        this.visitWidgets {
            it.isFocused = true
            this@SkyOceanScreen.addRenderableWidget(it)
        }
    }

    fun LayoutElement.applyAsRenderable() {
        this.visitWidgets {
            it.isFocused = true
            this@SkyOceanScreen.addRenderableOnly(it)
        }
    }

    fun LayoutElement.center() = this.apply {
        FrameLayout.centerInRectangle(this, 0, 0, this@SkyOceanScreen.width, this@SkyOceanScreen.height)
        if (this is Layout) {
            this.arrangeElements()
        }
    }

    fun Layout.asScrollable(width: Int, height: Int, init: LayoutWidget<FrameLayout>.() -> Unit = {}, allwaysShowScrollBar: Boolean = false): Layout {
        this.arrangeElements()
        val widget = LayoutWidget(this).also { it.visible = true }.withStretchToContentSize()

        return LayoutFactory.frame(width, height) {
            widget(widget.asScrollable(width, height, init, allwaysShowScrollBar))
        }
    }

    fun AbstractWidget.asScrollable(
        width: Int,
        height: Int,
        init: LayoutWidget<FrameLayout>.() -> Unit = {},
        allwaysShowScrollBar: Boolean = false,
    ): AbstractWidget {
        val scrollable = Widgets.frame { frame ->
            frame.withScrollableY(TriState.of(allwaysShowScrollBar.takeIf { it }))
                .withSize(width, this.height.coerceAtMost(height))
                .withContents { contents ->
                    contents.setMinWidth(width - 10)
                    contents.addChild(this, LayoutSettings.defaults().alignHorizontallyCenter())
                }.init()
        }

        return scrollable
    }
}
