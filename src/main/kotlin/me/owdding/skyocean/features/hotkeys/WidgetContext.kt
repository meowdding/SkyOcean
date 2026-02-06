package me.owdding.skyocean.features.hotkeys

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.hotkeys.actions.HotkeyActionType
import me.owdding.skyocean.features.hotkeys.conditions.HotkeyCondition
import me.owdding.skyocean.features.hotkeys.conditions.HotkeyConditionType
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.utils.components.CatppuccinColors
import me.owdding.skyocean.utils.extensions.createSprite
import me.owdding.skyocean.utils.extensions.createText
import me.owdding.skyocean.utils.extensions.middleLeft
import me.owdding.skyocean.utils.extensions.withPadding
import me.owdding.skyocean.utils.extensions.withTexturedBackground
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text

data class WidgetContext(
    val width: Int,
    val depth: Int = 0,
    val rebuildCallback: () -> Unit,
) {
    init {
        if (depth >= 5) throw UnsupportedOperationException("Depth higher then 5!")
    }

    var index: Int = 0

    val background: String
        get() {
            return "hotkey/$depth/background${if (index % 2 == 0) "_odd" else ""}"
        }

    val button: String = "hotkey/$depth/button"
    val listEntry: String = "hotkey/$depth/list_entry"
    val listBackground: String = "hotkey/$depth/list_background"

    fun advance() {
        index++
    }

    fun reset() {
        index = 0
    }

    fun createActionDropdown(state: DropdownState<HotkeyActionType>): LayoutElement = LayoutFactory.horizontal {
        createText("Type", CatppuccinColors.Mocha.surface0).withPadding(right = PADDING).add(middleLeft)
        val options = buildList {
            addAll(HotkeyActionType.entries)
            remove(HotkeyActionType.NONE)
        }
        createDropdown(state, options)
    }.withPadding(PADDING)

    fun <T : Enum<T>> LayoutBuilder.createDropdown(state: DropdownState<T>, options: List<T>) {
        val toText: (T) -> Component = { Text.of(it.name.toTitleCase(), CatppuccinColors.Mocha.surface0) }
        return Widgets.dropdown(
            state,
            options,
            toText,
            {
                it.withRenderer(
                    state.withRenderer { value, open ->
                        if (value == null) {
                            WidgetRenderers.ellpsisWithChevron<Button>(open)
                                .withColor(CatppuccinColors.Mocha.surface0Color)
                        } else {
                            WidgetRenderers.textWithChevron<Button>(
                                toText(value),
                                open,
                            ).withColor(CatppuccinColors.Mocha.surface0Color).withPadding(4, 6)
                        }
                    },
                )
                it.withSize(
                    PADDING * 15,
                    PADDING * 4,
                ).withTexture(createSprite(SkyOcean.id(button)))
            },
        ) {
            it.withSize(PADDING * 15, PADDING * 4 * options.size + PADDING).withTexture(SkyOcean.id(listBackground))
                .withEntrySprites(createSprite(SkyOcean.id(listEntry)))
        }.add(middleLeft)
    }

    fun createDropdown(state: DropdownState<HotkeyConditionType>): LayoutElement = LayoutFactory.horizontal {
        createText("Type", CatppuccinColors.Mocha.surface0).withPadding(right = PADDING).add(middleLeft)
        val options = buildList {
            addAll(HotkeyConditionType.entries)
            remove(HotkeyConditionType.NONE)
            if (depth + 1 >= 5) {
                removeIf { it.nested }
            }
        }
        createDropdown(state, options)
    }.withPadding(PADDING)

    fun push() = WidgetContext(width - PADDING * 2, depth + 1, rebuildCallback)
    fun createEntry(condition: HotkeyCondition?, callback: (HotkeyCondition) -> Unit): LayoutElement {
        if (condition == null) {
            val state = ListenableState.of(HotkeyConditionType.NONE)
            state.registerListener {
                callback(it.builder!!.invoke())
            }
            val dropdown = this.createDropdown(DropdownState(null, state, false))
            return LayoutFactory.frame(this.width) {
                dropdown.add(middleLeft)
            }.withTexturedBackground(this.background)
        } else {
            return condition.toWidget(consumer = callback)
        }
    }

    fun rebuild() = rebuildCallback()
}
