package me.owdding.skyocean.features.hotkeys.actions

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyocean.features.hotkeys.WidgetContext
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.extensions.withTexturedBackground
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.util.ExtraCodecs

interface HotkeyAction {

    val codec: MapCodec<out HotkeyAction>
    val type: HotkeyActionType

    fun perform()

    operator fun invoke() = perform()

    context(context: WidgetContext)
    fun asLayoutElement(selector: LayoutElement): LayoutElement

    context(context: WidgetContext)
    fun toWidget(consumer: (HotkeyAction) -> Unit): LayoutElement {
        val state: ListenableState<HotkeyActionType> = ListenableState.of(this.type)
        state.registerListener { newType ->
            if (newType == this.type) return@registerListener
            consumer(newType?.builder!!.invoke())
        }
        return asLayoutElement(
            context.createActionDropdown(
                DropdownState(null, state, false),
            ),
        ).withTexturedBackground(context.background)
    }
}

object KeybindActions {
    val ID_MAPPER = ExtraCodecs.LateBoundIdMapper<String, MapCodec<out HotkeyAction>>()

    @IncludedCodec
    val CODEC: Codec<HotkeyAction> = ID_MAPPER.codec(Codec.STRING).dispatch(HotkeyAction::codec) { it }

    init {
        ID_MAPPER.put("command", SkyOceanCodecs.CommandHotkeyActionCodec)
        //ID_MAPPER.put("key_mapping", SkyOceanCodecs.KeyMappingHotkeyActionCodec)
    }
}

enum class HotkeyActionType(val builder: (() -> HotkeyAction)? = null) {
    NONE,
    COMMAND({ CommandHotkeyAction("command") }),
    //KEY_MAPPING({ KeyMappingHotkeyAction("") }),
}
