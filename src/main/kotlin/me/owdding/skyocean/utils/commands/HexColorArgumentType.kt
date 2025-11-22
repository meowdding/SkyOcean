package me.owdding.skyocean.utils.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.skyocean.utils.chat.ChatUtils
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture
import kotlin.math.min

class HexColorArgumentType : ArgumentType<Int> {
    companion object {
        private val allChars = "1234567890ABCDEF".toCharArray()
    }

    private val invalidColor: DynamicCommandExceptionType = DynamicCommandExceptionType { id: Any? ->
        ChatUtils.prefix.copy().append("Color ") {
            append("$id") { this.color = TextColor.GOLD }
            append(" is invalid")
        }
    }


    override fun parse(reader: StringReader): Int {
        reader.expect('#')
        val cursor = reader.cursor
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip()
        }
        val string = reader.string.substring(cursor, reader.cursor).removePrefix("#")
        reader.cursor = min(cursor + 7, reader.cursor)
        if (string.length > 6) {
            throw invalidColor.createWithContext(reader, string)
        }

        return runCatching { string.toInt(16) }.getOrNull() ?: throw invalidColor.createWithContext(reader, string)
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val offsetBuilder = builder.createOffset(builder.start + builder.remainingLowerCase.length)
        if (builder.remainingLowerCase.isEmpty()) {
            offsetBuilder.suggest("#")
        } else if (builder.remainingLowerCase.startsWith('#') && builder.remainingLowerCase.length <= 6) {
            for (char in allChars) {
                offsetBuilder.suggest("$char")
            }
        }

        return offsetBuilder.buildFuture()
    }

}
