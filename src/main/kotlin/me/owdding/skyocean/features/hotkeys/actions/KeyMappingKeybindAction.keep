package me.owdding.skyocean.features.hotkeys.actions

import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.features.hotkeys.WidgetContext
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.extensions.topLeft
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.layouts.LayoutElement

@GenerateCodec
data class KeyMappingHotkeyAction(
    @FieldName("key_name") val keyName: String
) : HotkeyAction {
    override val codec: MapCodec<KeyMappingHotkeyAction> = SkyOceanCodecs.KeyMappingHotkeyActionCodec
    override val type: HotkeyActionType = HotkeyActionType.KEY_MAPPING

    context(context: WidgetContext)
    override fun asLayoutElement(selector: LayoutElement): LayoutElement = LayoutFactory.frame(context.width) {
        widget(selector, topLeft)
    }

    override fun perform() {
        KeyMapping.get(keyName)?.clickCount++
    }
}
