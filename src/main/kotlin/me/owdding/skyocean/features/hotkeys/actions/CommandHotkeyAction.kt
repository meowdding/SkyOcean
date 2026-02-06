package me.owdding.skyocean.features.hotkeys.actions

import com.mojang.serialization.MapCodec
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.OptionalBoolean
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.features.hotkeys.WidgetContext
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.components.CatppuccinColors
import me.owdding.skyocean.utils.extensions.bottomCenter
import me.owdding.skyocean.utils.extensions.createText
import me.owdding.skyocean.utils.extensions.createTextInput
import me.owdding.skyocean.utils.extensions.createToggleButton
import me.owdding.skyocean.utils.extensions.middleLeft
import me.owdding.skyocean.utils.extensions.topLeft
import me.owdding.skyocean.utils.extensions.withPadding
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.helpers.McClient

@GenerateCodec
data class CommandHotkeyAction(
    var command: String,
    @OptionalBoolean(false) @FieldName("allow_client_commands") var allowClientCommands: Boolean = false,
) : HotkeyAction {
    override val codec: MapCodec<CommandHotkeyAction> = SkyOceanCodecs.CommandHotkeyActionCodec
    override val type: HotkeyActionType = HotkeyActionType.COMMAND

    context(context: WidgetContext)
    override fun asLayoutElement(selector: LayoutElement): LayoutElement = LayoutFactory.frame(context.width) {
        vertical(PADDING) {
            widget(selector, topLeft)
            LayoutFactory.vertical {
                createText("Allow Client Commands", CatppuccinColors.Mocha.surface0).withPadding(left = PADDING).add(middleLeft)
                val state = ListenableState.of(allowClientCommands)
                state.registerListener {
                    allowClientCommands = it
                }
                createToggleButton(
                    state,
                    trueText = "Yes",
                    falseText = "No",
                    falseColor = CatppuccinColors.Mocha.mauve,
                    texture = id(context.listBackground),
                    width = context.width / 3,
                    onClick = context.rebuildCallback,
                ).add(middleLeft)
            }.add(middleLeft)
            LayoutFactory.vertical {
                createText("Command (without /)", CatppuccinColors.Mocha.surface0).withPadding(left = PADDING).add()
                val state = ListenableState.of(command)
                state.registerListener {
                    command = it
                }
                createTextInput(
                    state = state,
                    textColor = CatppuccinColors.Mocha.surface0Color,
                    placeholder = "Command",
                    texture = id(context.listBackground),
                    width = context.width - PADDING * 2,
                ).add()
            }.withPadding(bottom = PADDING).add(bottomCenter)
        }
    }

    override fun perform() {
        if (allowClientCommands) {
            McClient.sendClientCommand(command)
        } else {
            McClient.sendCommand(command)
        }
    }
}
