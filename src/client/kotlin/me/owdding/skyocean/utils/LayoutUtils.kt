package me.owdding.skyocean.utils

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import com.teamresourceful.resourcefullib.common.utils.TriState
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.Text

abstract class SkyOceanScreen(title: Component = CommonComponents.EMPTY) : BaseCursorScreen(title) {
    constructor(title: String) : this(Text.of(title))

    fun olympus(path: String) = SkyOcean.olympus(path)

    fun LayoutElement.applyLayout() {
        this.visitWidgets {
            it.isFocused = true
            this@SkyOceanScreen.addRenderableWidget(it)
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

    fun Layout.asScrollableWidget(
        width: Int,
        height: Int,
        init: LayoutWidget<FrameLayout>.() -> Unit = {},
        allwaysShowScrollBar: Boolean = false,
    ): LayoutWidget<FrameLayout> {
        this.arrangeElements()
        val widget = LayoutWidget(this).also { it.visible = true }.withStretchToContentSize()

        return widget.asScrollable(width, height, init, allwaysShowScrollBar)
    }

    fun AbstractWidget.asScrollable(
        width: Int,
        height: Int,
        init: LayoutWidget<FrameLayout>.() -> Unit = {},
        allwaysShowScrollBar: Boolean = false,
    ): LayoutWidget<FrameLayout> {
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

fun List<List<LayoutElement>>.asTable(spacing: Int = 0): Layout {
    return LayoutFactory.vertical(spacing) {
        this@asTable.map {
            LayoutFactory.horizontal(spacing) {
                it.forEach(::widget)
            }
        }.forEach(::widget)
    }
}
