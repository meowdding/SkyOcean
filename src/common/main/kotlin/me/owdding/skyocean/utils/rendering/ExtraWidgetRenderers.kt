package me.owdding.skyocean.utils.rendering

import com.teamresourceful.resourcefullib.common.color.Color
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.lib.displays.Display
import me.owdding.skyocean.utils.Utils.not
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component

object ExtraWidgetRenderers {

    fun <T : AbstractWidget> text(component: Component, color: Color = MinecraftColors.WHITE): WidgetRenderer<T> {
        return WidgetRenderers.text<T>(component).withColor(color)
    }

    fun <T : AbstractWidget> text(component: String, color: Color = MinecraftColors.WHITE): WidgetRenderer<T> {
        return WidgetRenderers.text<T>(!component).withColor(color)
    }

    fun <T : AbstractWidget> conditional(onTrue: WidgetRenderer<T>, onFalse: WidgetRenderer<T>, supplier: () -> Boolean): WidgetRenderer<T> =
        WidgetRenderer<T> { graphics, context, partial ->
            val widget = if (supplier()) onTrue else onFalse
            widget.render(graphics, context, partial)
        }

    fun <T : AbstractWidget> display(display: Display): WidgetRenderer<T> = WidgetRenderer<T> { graphics, ctx, _ -> display.render(graphics, ctx.x, ctx.y) }
}
