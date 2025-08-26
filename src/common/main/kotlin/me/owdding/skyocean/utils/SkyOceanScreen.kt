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
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.Text
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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

    private val zeroDelay = (-1).seconds.toJavaDuration()

    fun <T : AbstractWidget> T.withoutTooltipDelay(): T = apply {
        this.setTooltipDelay(zeroDelay)
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

    fun Layout.asScrollable(width: Int, height: Int, init: LayoutWidget<FrameLayout>.() -> Unit = {}, alwaysShowScrollBar: Boolean = false): Layout {
        this.arrangeElements()
        val widget = LayoutWidget(this).apply {
            visible = true
            withAutoFocus(false)
        }.withStretchToContentSize()

        return LayoutFactory.frame(width, height) {
            widget(widget.asScrollable(width, height, init, alwaysShowScrollBar))
        }
    }

    fun Layout.asScrollableWidget(
        width: Int,
        height: Int,
        init: LayoutWidget<FrameLayout>.() -> Unit = {},
        alwaysShowScrollBar: Boolean = false,
    ): LayoutWidget<FrameLayout> {
        this.arrangeElements()
        val widget = LayoutWidget(this).apply {
            visible = true
            withAutoFocus(false)
        }.withStretchToContentSize()

        return widget.asScrollable(width, height, init, alwaysShowScrollBar)
    }

    fun AbstractWidget.asScrollable(
        width: Int,
        height: Int,
        init: LayoutWidget<FrameLayout>.() -> Unit = {},
        alwaysShowScrollBar: Boolean = false,
    ): LayoutWidget<FrameLayout> {
        val scrollable = Widgets.frame { frame ->
            frame.withScrollableY(TriState.of(alwaysShowScrollBar.takeIf { it }))
                .withSize(width, this.height.coerceAtMost(height))
                .withAutoFocus(false)
                .withContents { contents ->
                    contents.setMinWidth(width - 10)
                    contents.addChild(this, LayoutSettings.defaults().alignHorizontallyCenter())
                }
                .withAutoFocus(false)
                .init()
        }

        return scrollable
    }
}

abstract class SkyOceanPopupScreen(val parent: Screen? = McScreen.self, title: Component = CommonComponents.EMPTY) : SkyOceanScreen(title) {

    override fun onClose() {
        super.onClose()
        McClient.setScreenAsync { parent }
    }

    override fun removed() {
        super.removed()
        McClient.setScreenAsync { parent }
    }

}
