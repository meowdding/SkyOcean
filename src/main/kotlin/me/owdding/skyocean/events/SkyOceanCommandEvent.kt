package me.owdding.skyocean.events

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.CommandBuildContext
import tech.thatgravyboat.skyblockapi.api.events.misc.AbstractModRegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.LiteralCommandBuilder

class RegisterSkyOceanCommandEvent(
    val dispatcher: CommandDispatcher<FabricClientCommandSource>,
    val context: CommandBuildContext,
) : AbstractModRegisterCommandsEvent(RegisterCommandsEvent(dispatcher), "skyocean", "so") {

    fun registerDevWithCallback(command: String, callback: CommandContext<FabricClientCommandSource>.() -> Unit) {
        registerWithCallback("dev $command", callback)
    }

    fun registerDev(command: String, builder: LiteralCommandBuilder.() -> Unit) {
        register("dev $command", builder)
    }

}
