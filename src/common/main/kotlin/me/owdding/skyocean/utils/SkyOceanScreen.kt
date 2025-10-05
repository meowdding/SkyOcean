package me.owdding.skyocean.utils

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import earth.terrarium.olympus.client.components.dropdown.DropdownBuilder
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.lib.platform.screens.MeowddingScreen
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.Text


abstract class SkyOceanScreen(title: Component = CommonComponents.EMPTY) : MeowddingScreen(title) {
    constructor(title: String) : this(Text.of(title))

    fun olympus(path: String) = SkyOcean.olympus(path)

    fun LayoutElement.applyLayout() {
        this.visitWidgets {
            it.isFocused = true
            this@SkyOceanScreen.addRenderableWidget(it)
        }
    }

    fun LayoutElement.applyAndGetElements(): MutableList<AbstractWidget> {
        this.applyLayout()
        val elements = mutableListOf<AbstractWidget>()
        this.visitWidgets(elements::add)
        return elements
    }

    fun LayoutElement.applyAsRenderable() {
        this.visitWidgets {
            it.isFocused = true
            this@SkyOceanScreen.addRenderableOnly(it)
        }
    }

    fun LayoutElement.center() = this.apply {
        FrameLayout.centerInRectangle(this, 0, 0, this@SkyOceanScreen.width, this@SkyOceanScreen.height)
        if (this is Layout) {
            this.arrangeElements()
        }
    }


    fun Layout.asLayoutWidget(init: LayoutWidget<Layout>.() -> Unit = {}) = LayoutWidget(this).apply {
        visible = true
        withAutoFocus(false)
        init()
    }

    fun <T> dropdown(
        state: DropdownState<T>,
        options: MutableList<T>,
        optionText: (T) -> Component,
        factory: Button.() -> Unit,
        builder: DropdownBuilder<T>.() -> Unit,
        optionFactory: (T) -> WidgetRenderer<Button>,
    ): Button {


        val button: Button = Widgets.button { btn ->
            btn.withRenderer(
                state.withRenderer { value, open ->
                    (if (value == null) WidgetRenderers.ellpsisWithChevron(open) else WidgetRenderers.textWithChevron<Button>(
                        optionText(value),
                        open,
                    )).withPadding(4, 6)
                },
            )
        }
        button.factory()

        val dropdown = button.withDropdown(state)
        dropdown.withOptions(options).withEntryRenderer(optionFactory)

        dropdown.builder()
        return dropdown.build()
    }
}
