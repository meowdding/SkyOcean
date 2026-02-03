package me.owdding.skyocean.features.hotkeys.conditions

import com.mojang.serialization.MapCodec
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.features.hotkeys.WidgetContext
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.components.CatppuccinColors
import me.owdding.skyocean.utils.extensions.bottomCenter
import me.owdding.skyocean.utils.extensions.createMultiselectDropdown
import me.owdding.skyocean.utils.extensions.createSprite
import me.owdding.skyocean.utils.extensions.createText
import me.owdding.skyocean.utils.extensions.framed
import me.owdding.skyocean.utils.extensions.topLeft
import me.owdding.skyocean.utils.extensions.withPadding
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent

@GenerateCodec
data class IslandHotkeyCondition(
    @Compact val island: MutableSet<SkyBlockIsland> = mutableSetOf(),
) : HotkeyCondition {
    override val codec: MapCodec<out HotkeyCondition> = SkyOceanCodecs.IslandHotkeyConditionCodec
    override val type: HotkeyConditionType = HotkeyConditionType.ISLAND

    override fun test(): Boolean = SkyBlockIsland.inAnyIsland(island)

    context(context: WidgetContext)
    override fun asLayoutElement(selector: LayoutElement): LayoutElement = LayoutFactory.vertical(PADDING) {
        spacer(context.width)
        widget(selector, topLeft)

        LayoutFactory.vertical {
            spacer(context.width)
            createText("Islands", CatppuccinColors.Mocha.surface0).withPadding(left = PADDING).add()

            val state: ListenableState<Set<SkyBlockIsland>> = ListenableState.of(island)
            state.registerListener {
                island.clear()
                island.addAll(it)
            }
            createMultiselectDropdown(
                state,
                SkyBlockIsland.entries,
                { it.name.toTitleCase().asComponent() },
                elementColor = CatppuccinColors.Mocha.surface0Color,
                buttonTexture = id(context.button),
                backgroundTexture = id(context.listBackground),
                elementSprite = createSprite(id(context.listEntry))
            ) {
                withSize(context.width / 2, 20)
            }.withPadding(left = PADDING).add()
        }.withPadding(bottom = PADDING).add(bottomCenter)
    }.framed(context.width)
}
