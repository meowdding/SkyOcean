package me.owdding.skyocean.features.keybinds.actions

import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import tech.thatgravyboat.skyblockapi.helpers.McClient

@GenerateCodec
data class CommandKeybindAction(
    val command: String,
    @FieldName("allow_client_commands") val allowClientCommands: Boolean
) : KeybindAction {
    override val codec: MapCodec<CommandKeybindAction> = SkyOceanCodecs.CommandKeybindActionCodec

    override fun perform() {
        if (allowClientCommands) {
            McClient.sendClientCommand(command)
        } else {
            McClient.sendCommand(command)
        }
    }
}
