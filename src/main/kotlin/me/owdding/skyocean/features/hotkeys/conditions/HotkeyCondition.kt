package me.owdding.skyocean.features.hotkeys.conditions

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

interface HotkeyCondition {
    val codec: MapCodec<out HotkeyCondition>
    val type: HotkeyConditionType
    fun test(): Boolean

    operator fun invoke() = test()

    context(context: WidgetContext)
    fun asLayoutElement(selector: LayoutElement): LayoutElement

    context(context: WidgetContext)
    fun toWidget(dropdownModifier: (LayoutElement) -> LayoutElement = { it }, consumer: (HotkeyCondition) -> Unit): LayoutElement {
        val state: ListenableState<HotkeyConditionType> = ListenableState.of(this.type)
        state.registerListener { newType ->
            if (newType == this.type) return@registerListener
            consumer(newType?.builder!!.invoke())
        }
        return asLayoutElement(
            dropdownModifier(
                context.createDropdown(
                    DropdownState(null, state, false),
                ),
            ),
        ).withTexturedBackground(context.background)
    }
}

data object HotkeyConditions {
    val idMapper = ExtraCodecs.LateBoundIdMapper<String, MapCodec<out HotkeyCondition>>()

    @IncludedCodec
    val CODEC: Codec<HotkeyCondition> = idMapper.codec(Codec.STRING).dispatch(HotkeyCondition::codec) { it }

    init {
        idMapper.put("always", AlwaysHotkeyCondition.codec)
        idMapper.put("island", SkyOceanCodecs.IslandHotkeyConditionCodec)
        idMapper.put("not", SkyOceanCodecs.NotHotkeyConditionCodec)
        idMapper.put("and", SkyOceanCodecs.AndHotkeyConditionCodec)
        idMapper.put("or", SkyOceanCodecs.OrHotkeyConditionCodec)
        idMapper.put("dungeon_floor", SkyOceanCodecs.DungeonFloorHotkeyConditionCodec)
        idMapper.put("dungeon_class", SkyOceanCodecs.DungeonClassHotkeyConditionCodec)
    }
}

enum class HotkeyConditionType(val builder: (() -> HotkeyCondition)? = null, val nested: Boolean = false) {
    NONE,
    ALWAYS(builder = { AlwaysHotkeyCondition }),
    ISLAND(builder = ::IslandHotkeyCondition),
    NOT(builder = { NotHotkeyCondition(AlwaysHotkeyCondition) }, nested = true),
    AND(builder = ::AndHotkeyCondition, nested = true),
    OR(builder = ::OrHotkeyCondition, nested = true),
    DUNGEON_FLOOR(builder = ::DungeonFloorHotkeyCondition),
    DUNGEON_CLASS(builder = ::DungeonClassHotkeyCondition),
    ;

}
