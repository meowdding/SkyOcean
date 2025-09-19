package me.owdding.skyocean.utils.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.suggestions.SkyOceanSuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ResourceLocationException
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture

class VirtualResourceArgument(
    private val locations: Collection<ResourceLocation>,
    private val namespace: String = ResourceLocation.DEFAULT_NAMESPACE,
) : ArgumentType<ResourceLocation>, SkyOceanSuggestionProvider {

    private val commandException: SimpleCommandExceptionType = SimpleCommandExceptionType(Component.translatable("argument.id.invalid"))
    private val identifierNotFound: DynamicCommandExceptionType = DynamicCommandExceptionType { id: Any? ->
        ChatUtils.prefix.copy().append("Identifier ") {
            append("$id") { this.color = TextColor.GOLD }
            append(" not found")
        }
    }

    override fun parse(reader: StringReader): ResourceLocation {
        val resourceLocation: ResourceLocation = this.fromCommandInput(reader)
        if (!locations.contains(resourceLocation)) {
            throw identifierNotFound.create(resourceLocation)
        }

        return resourceLocation
    }

    @Throws(CommandSyntaxException::class)
    private fun fromCommandInput(reader: StringReader): ResourceLocation {
        val i = reader.cursor
        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip()
        }
        val string = reader.string.substring(i, reader.cursor)
        try {
            val split: Array<String> = split(string)
            return ResourceLocation.fromNamespaceAndPath(split[0], split[1])
        } catch (_: ResourceLocationException) {
            reader.cursor = i
            throw commandException.createWithContext(reader)
        }
    }

    private fun split(id: String): Array<String> {
        val strings = arrayOf(this.namespace, id)
        val i = id.indexOf(':')
        if (i >= 0) {
            strings[1] = id.substring(i + 1)
            if (i >= 1) {
                strings[0] = id.substring(0, i)
            }
        }
        return strings
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        locations.forEach {
            suggest(builder, it.toString())
        }
        return builder.buildFuture()
    }

    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder,
    ) = listSuggestions(context, builder)
}
