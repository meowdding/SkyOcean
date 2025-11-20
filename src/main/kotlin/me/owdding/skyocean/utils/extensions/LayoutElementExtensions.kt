package me.owdding.skyocean.utils.extensions

import com.teamresourceful.resourcefullib.common.utils.TriState
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.layouts.PaddedWidget
import me.owdding.skyocean.accessors.ClearableLayout
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun Layout.tryClear() = apply {
    (this as? ClearableLayout)?.`skyocean$clear`()
}

fun <T : Layout> LayoutWidget<T>.clear() = apply { withContents { it.tryClear() } }

fun LayoutWidget<FrameLayout>.setFrameContent(content: LayoutElement) = apply { clear().withContents { it.addChild(content) } }
fun LayoutWidget<LinearLayout>.setLayoutContent(content: LayoutElement) = apply { clear().withContents { it.addChild(content) } }

fun LayoutElement.withPadding(
    padding: Int,
    top: Int = padding,
    right: Int = padding,
    bottom: Int = padding,
    left: Int = padding,
): AbstractWidget = PaddedWidget(this, top, right, bottom, left)

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

private val zeroDelay = (-1).seconds.toJavaDuration()

fun <T : AbstractWidget> T.withoutTooltipDelay(): T = apply {
    this.setTooltipDelay(zeroDelay)
}
