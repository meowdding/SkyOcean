package me.owdding.skyocean.features.text

import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.chat.ChatConfig
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import kotlin.experimental.and
import kotlin.experimental.or

@Module
object MarkdownChat : MeowddingLogger by SkyOcean.featureLogger() {
    const val ZERO: Byte = 0
    const val MINUS_ONE: Byte = -1

    private val enabled get() = ChatConfig.markdownChat

    fun tryModify(sequence: FormattedText): FormattedText {
        if (!enabled) return sequence
        try {
            val string = sequence.string.stripColor()
            val array = Array(string.length) { 0.toByte() }
            findSections(array, 0, string, string.length - 1, 0)

            val sequence = Language.getInstance().getVisualOrder(sequence)

            return FormattedCharSequence { sink ->
                var offset = 0
                var accumulator = 0
                sequence.accept { position, style, codepoint ->
                    val modifier = array.getOrNull(accumulator++) ?: 0
                    val style = when (modifier) {
                        ZERO -> {
                            style
                        }

                        MINUS_ONE -> {
                            offset++
                            return@accept true
                        }

                        else -> {
                            style
                                .withBold(modifier.and(Formattings.BOLD.mask) == Formattings.BOLD.mask || style.isBold)
                                .withItalic(modifier.and(Formattings.ITALIC.mask) == Formattings.ITALIC.mask || style.isItalic)
                                .withUnderlined(modifier.and(Formattings.UNDERLINE.mask) == Formattings.UNDERLINE.mask || style.isUnderlined)
                                .withStrikethrough(modifier.and(Formattings.STRIKETHROUGH.mask) == Formattings.STRIKETHROUGH.mask || style.isStrikethrough)
                        }
                    }

                    sink.accept((position - offset).coerceAtLeast(0), style, codepoint)
                }
            }.toComponent()
        } catch (e: Exception) {
            error("Failed to process markdown", e)
            return sequence
        }
    }


    fun FormattedCharSequence.toComponent(): Component {
        val result: MutableComponent = Text.of()
        var currentStyle: Style? = null
        var current = Text.of()
        var builder = StringBuilder()

        this.accept { _, style, codepoint ->
            if (currentStyle == null) {
                currentStyle = style
                current.style = style
            }
            if (currentStyle != style) {
                currentStyle = style
                current.append(builder.toString())
                result.append(current)
                builder = StringBuilder()
                current = Text.of()
                current.style = currentStyle
            }
            builder.appendCodePoint(codepoint)

            true
        }

        current.append(builder.toString())
        current.style = currentStyle ?: Style.EMPTY
        result.append(current)
        return result
    }

    fun findSections(modifiers: Array<Byte>, cursor: Int, content: String, spanEnd: Int, modifier: Byte) {
        var previous: Char? = null
        var skip = 0
        for ((index, ch) in content.substring(cursor..spanEnd.coerceAtMost(content.length - 1)).withIndex()) {
            val current = cursor + index
            if (current <= skip) continue
            modifiers[current] = modifier

            var untilNext: String? = null
            var formatting: Byte? = null
            when (ch) {
                '*' if previous == '*' -> {
                    untilNext = "**"
                    formatting = Formattings.BOLD.mask
                }
                '_' if previous == '_' -> {
                    untilNext = "__"
                    formatting = Formattings.UNDERLINE.mask
                }
                '|' if previous == '|' -> {
                    untilNext = "||"
                    formatting = Formattings.OBFUSCATED.mask
                }
                '~' if previous == '~' -> {
                    untilNext = "~~"
                    formatting = Formattings.STRIKETHROUGH.mask
                }
                else if previous == '*' -> {
                    untilNext = "*"
                    formatting = Formattings.ITALIC.mask
                }
                else if previous == '_' -> {
                    untilNext = "_"
                    formatting = Formattings.ITALIC.mask
                }
                else -> {
                    previous = ch
                }
            }

            if (untilNext != null && formatting != null) {
                previous = null
                val end = content.indexOf(untilNext, current)
                if (end == -1 || end > spanEnd) {
                    continue
                }

                skip = end + untilNext.length - 1
                if (untilNext.length > 1) {
                    modifiers[current] = -1
                    modifiers[end + 1] = -1
                }
                modifiers[current - 1] = -1
                findSections(modifiers, current + untilNext.length - 1, content, end - 1, (modifier or formatting))
                modifiers[end] = -1
            }
        }
    }

    enum class Formattings {
        ITALIC,
        BOLD,
        UNDERLINE,
        STRIKETHROUGH,
        OBFUSCATED,
        ;

        val mask = (1 shl ordinal).toByte()
    }

}
