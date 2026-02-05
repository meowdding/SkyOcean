package me.owdding.skyocean.features.hotkeys.conditions

import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.features.hotkeys.WidgetContext
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.utils.components.CatppuccinColors
import me.owdding.skyocean.utils.extensions.bottomCenter
import me.owdding.skyocean.utils.extensions.createMultiselectDropdown
import me.owdding.skyocean.utils.extensions.createSprite
import me.owdding.skyocean.utils.extensions.createText
import me.owdding.skyocean.utils.extensions.framed
import me.owdding.skyocean.utils.extensions.topLeft
import me.owdding.skyocean.utils.extensions.withPadding
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component

interface SelectHotkeyCondition<DataType> : HotkeyCondition {

    val text: String

    fun data(): MutableSet<DataType>
    fun possibilities(): List<DataType>
    fun nameConverter(data: DataType): Component

    context(context: WidgetContext)
    override fun asLayoutElement(selector: LayoutElement): LayoutElement = LayoutFactory.vertical(PADDING) {
        spacer(context.width)
        widget(selector, topLeft)

        LayoutFactory.vertical {
            spacer(context.width)
            createText(text, CatppuccinColors.Mocha.surface0).withPadding(left = PADDING).add()

            val state: ListenableState<Set<DataType>> = ListenableState.of(data())
            state.registerListener {
                data().clear()
                data().addAll(it)
            }
            createMultiselectDropdown(
                state,
                possibilities(),
                { nameConverter(it) },
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
