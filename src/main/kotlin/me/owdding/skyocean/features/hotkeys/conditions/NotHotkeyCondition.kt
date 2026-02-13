package me.owdding.skyocean.features.hotkeys.conditions

import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.features.hotkeys.WidgetContext
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.extensions.bottomCenter
import me.owdding.skyocean.utils.extensions.createText
import me.owdding.skyocean.utils.extensions.framed
import me.owdding.skyocean.utils.extensions.topLeft
import me.owdding.skyocean.utils.extensions.withPadding
import net.minecraft.client.gui.layouts.LayoutElement

@GenerateCodec
data class NotHotkeyCondition(
    var child: HotkeyCondition,
) : HotkeyCondition {
    override val codec: MapCodec<out HotkeyCondition> = SkyOceanCodecs.NotHotkeyConditionCodec
    override val type: HotkeyConditionType = HotkeyConditionType.NOT

    override fun test(): Boolean = !child.test()

    context(context: WidgetContext)
    override fun asLayoutElement(selector: LayoutElement): LayoutElement = LayoutFactory.vertical(PADDING) {
        spacer(context.width)
        widget(selector, topLeft)

        createText("Child", CatppuccinColors.Mocha.surface0).withPadding(left = PADDING).add()
        context(context.push()) {
            child.toWidget {
                child = it
                context.rebuild()
            }.withPadding(bottom = PADDING).add(bottomCenter)
        }
    }.framed(context.width)
}
