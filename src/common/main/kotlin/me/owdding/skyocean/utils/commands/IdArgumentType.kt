package me.owdding.skyocean.utils.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.suggestions.SkyOceanSuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture

data class IdArgumentType(val entries: List<String>) : ArgumentType<String>, SkyOceanSuggestionProvider {
    private val identifierNotFound: DynamicCommandExceptionType = DynamicCommandExceptionType { id: Any? ->
        ChatUtils.prefix.copy().append("Identifier ") {
            append("$id") { this.color = TextColor.GOLD }
            append(" not found")
        }
    }

    override fun parse(reader: StringReader): String {
        val cursor = reader.cursor
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip()
        }
        val string = reader.string.substring(cursor, reader.cursor)

        return entries.find { it == string.lowercase() } ?: run {
            throw identifierNotFound.createWithContext(reader, string)
        }
    }


    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        entries.forEach {
            suggest(builder, it.lowercase())
        }
        return builder.buildFuture()
    }

    override fun getSuggestions(
        p0: CommandContext<FabricClientCommandSource>,
        p1: SuggestionsBuilder,
    ) = listSuggestions(p0, p1)
}
