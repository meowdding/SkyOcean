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
import net.minecraft.commands.SharedSuggestionProvider
import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent

typealias LiteralCommandBuilder = CommandBuilder<LiteralArgumentBuilder<FabricClientCommandSource>>
typealias ArgumentCommandBuilder<T> = CommandBuilder<RequiredArgumentBuilder<FabricClientCommandSource, T>>


class RegisterSkyOceanCommandEvent(
    private val dispatcher: CommandDispatcher<FabricClientCommandSource>,
    private val context: CommandBuildContext,
) : SkyBlockEvent() {

    fun register(command: LiteralArgumentBuilder<FabricClientCommandSource>) {
        ClientCommandManager.literal("skyocean")
            .then(command)
            .let(dispatcher::register)
    }

    fun register(command: String, builder: LiteralCommandBuilder.() -> Unit) {
        ClientCommandManager.literal("skyocean")
            ?.apply { LiteralCommandBuilder(this, context).then(command, action = builder) }
            ?.let(dispatcher::register)
    }

    fun registerWithCallback(command: String, callback: CommandContext<FabricClientCommandSource>.() -> Unit) {
        register(command) {
            this.callback(callback)
        }
    }

    fun registerDev(command: LiteralArgumentBuilder<FabricClientCommandSource>) {
        register(ClientCommandManager.literal("dev").then(command))
    }

    fun registerDevWithCallback(command: String, callback: CommandContext<FabricClientCommandSource>.() -> Unit) {
        registerDev(command) {
            this.callback(callback)
        }
    }

    fun registerDev(command: String, builder: LiteralCommandBuilder.() -> Unit) {
        register(
            ClientCommandManager.literal("dev").apply {
                LiteralCommandBuilder(this, context).then(command, action = builder)
            },
        )
    }

}

class CommandBuilder<B : ArgumentBuilder<FabricClientCommandSource, B>> internal constructor(
    private val builder: ArgumentBuilder<FabricClientCommandSource, B>,
    private val context: CommandBuildContext,
) {

    fun callback(callback: CommandContext<FabricClientCommandSource>.() -> Unit) {
        this.builder.executes {
            callback(it)
            1
        }
    }

    fun then(vararg names: String, action: LiteralCommandBuilder.() -> Unit): CommandBuilder<B> {
        for (name in names) {
            if (name.contains(" ")) {
                val builder = CommandBuilder(ClientCommandManager.literal(name.substringBefore(" ")), context)
                builder.then(name.substringAfter(" "), action = action)
                this.builder.then(builder.builder)
                continue
            }
            val builder = CommandBuilder(ClientCommandManager.literal(name), context)
            builder.action()
            this.builder.then(builder.builder)
        }
        return this
    }

    fun thenCallback(vararg name: String, callback: CommandContext<FabricClientCommandSource>.() -> Unit) {
        then(*name) {
            callback(callback)
        }
    }

    fun <T> then(
        name: String,
        argument: ArgumentType<T>,
        suggestions: Collection<String>,
        action: ArgumentCommandBuilder<T>.() -> Unit,
    ): CommandBuilder<B> = then(
        name,
        argument,
        { _, builder -> SharedSuggestionProvider.suggest(suggestions, builder) },
        action,
    )

    fun <T> then(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<FabricClientCommandSource>? = null,
        action: ArgumentCommandBuilder<T>.() -> Unit,
    ): CommandBuilder<B> {
        if (name.contains(" ")) {
            val builder = CommandBuilder(ClientCommandManager.literal(name.substringBefore(" ")), context)
            builder.then(name.substringAfter(" "), argument, suggestions, action)
            this.builder.then(builder.builder)
            return this
        }
        val builder = CommandBuilder(
            ClientCommandManager.argument(name, argument).apply {
                if (suggestions != null) suggests(suggestions)
            },
            context,
        )
        builder.action()
        this.builder.then(builder.builder)
        return this
    }

    fun <T> thenCallback(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<FabricClientCommandSource>? = null,
        callback: CommandContext<FabricClientCommandSource>.() -> Unit,
    ): CommandBuilder<B> = then(name, argument, suggestions) {
        callback(callback)
    }

    fun context() = context
}
