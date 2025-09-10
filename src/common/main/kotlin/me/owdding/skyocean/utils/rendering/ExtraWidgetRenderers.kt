package me.owdding.skyocean.utils.rendering

import com.teamresourceful.resourcefullib.common.color.Color
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
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

}
