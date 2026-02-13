package me.owdding.skyocean.features.hotkeys.conditions

import com.mojang.serialization.MapCodec
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.features.hotkeys.WidgetContext
import me.owdding.skyocean.utils.extensions.*
import net.minecraft.client.gui.layouts.LayoutElement

data object AlwaysHotkeyCondition : HotkeyCondition {
    override val codec: MapCodec<out HotkeyCondition> = MapCodec.unit { AlwaysHotkeyCondition }
    override val type: HotkeyConditionType = HotkeyConditionType.ALWAYS

    override fun test(): Boolean = true
    context(context: WidgetContext)
    override fun asLayoutElement(selector: LayoutElement): LayoutElement = LayoutFactory.frame(context.width) {
        widget(selector, topLeft)
    }
}
