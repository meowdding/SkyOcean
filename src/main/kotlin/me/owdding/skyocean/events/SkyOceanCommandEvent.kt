package me.owdding.skyocean.events

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.CommandBuildContext
import tech.thatgravyboat.skyblockapi.api.events.misc.AbstractModRegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.CommandBuilder
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.ArgumentCommandBuilder as SbApiArgumentBuilder
import tech.thatgravyboat.skyblockapi.api.events.misc.LiteralCommandBuilder as SbApiLiteralBuilder


typealias LiteralCommandBuilder = SkyOceanCommandBuilder<LiteralArgumentBuilder<FabricClientCommandSource>>
typealias ArgumentCommandBuilder<T> = SkyOceanCommandBuilder<RequiredArgumentBuilder<FabricClientCommandSource, T>>


class RegisterSkyOceanCommandEvent(
    val dispatcher: CommandDispatcher<FabricClientCommandSource>,
    val context: CommandBuildContext,
) : AbstractModRegisterCommandsEvent(RegisterCommandsEvent(dispatcher), "skyocean") {
    private val prefixes = listOf("skyocean", "so")

    fun register(command: LiteralArgumentBuilder<FabricClientCommandSource>) = prefixes.forEach {
        ClientCommandManager.literal(it)
            .then(command)
            .let(dispatcher::register)
    }

    override fun registerBaseCallback(callback: CommandContext<FabricClientCommandSource>.() -> Unit) = prefixes.forEach {
        register(it) {
            callback(callback)
        }
    }

    override fun register(command: String, builder: SbApiLiteralBuilder.() -> Unit) = prefixes.forEach {
        ClientCommandManager.literal(it)
            .apply { LiteralCommandBuilder(this, context).then(command, action = builder) }
            .let(dispatcher::register)
    }

    fun registerDevWithCallback(command: String, callback: CommandContext<FabricClientCommandSource>.() -> Unit) {
        registerWithCallback("dev $command", callback)
    }

    fun registerDev(command: String, builder: SbApiLiteralBuilder.() -> Unit) {
        register("dev $command", builder)
    }

}

class SkyOceanCommandBuilder<B : ArgumentBuilder<FabricClientCommandSource, B>> internal constructor(
    builder: ArgumentBuilder<FabricClientCommandSource, B>,
    private val context: CommandBuildContext,
) : CommandBuilder<B>(builder) {
    override fun then(vararg names: String, action: SbApiLiteralBuilder.() -> Unit): SkyOceanCommandBuilder<B> {
        for (name in names) {
            if (name.contains(" ")) {
                val builder = SkyOceanCommandBuilder(ClientCommandManager.literal(name.substringBefore(" ")), context)
                builder.then(name.substringAfter(" "), action = action)
                this.builder.then(builder.builder)
                continue
            }
            val builder = SkyOceanCommandBuilder(ClientCommandManager.literal(name), context)
            builder.action()
            this.builder.then(builder.builder)
        }
        return this
    }

    override fun <T> then(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<FabricClientCommandSource>?,
        action: SbApiArgumentBuilder<T>.() -> Unit,
    ): CommandBuilder<B> {
        if (name.contains(" ")) {
            val builder = SkyOceanCommandBuilder(ClientCommandManager.literal(name.substringBefore(" ")), context)
            builder.then(name.substringAfter(" "), argument, suggestions, action)
            this.builder.then(builder.builder)
            return this
        }
        val builder = SkyOceanCommandBuilder(
            ClientCommandManager.argument(name, argument).apply {
                if (suggestions != null) suggests(suggestions)
            },
            context,
        )
        builder.action()
        this.builder.then(builder.builder)
        return this
    }

    fun context() = context
}

