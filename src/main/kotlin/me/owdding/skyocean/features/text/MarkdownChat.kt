package me.owdding.skyocean.features.text

import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.chat.ChatConfig
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.substring
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
            val array = Array(string.length) { ZERO }
            findSections(array, 0, string, string.length - 1, 0)

            val sequence = Language.getInstance().getVisualOrder(sequence)

            return FormattedCharSequence { sink ->
                var offset = 0
                var accumulator = 0
                var obfuscatedStart: Int? = null
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
                                .withBold(Formattings.BOLD.isActive(modifier) || style.isBold)
                                .withItalic(Formattings.ITALIC.isActive(modifier) || style.isItalic)
                                .withUnderlined(Formattings.UNDERLINE.isActive(modifier) || style.isUnderlined)
                                .withStrikethrough(Formattings.STRIKETHROUGH.isActive(modifier) || style.isStrikethrough)
                                .let {
                                    if (Formattings.OBFUSCATED.isActive(modifier)) {
                                        val start = obfuscatedStart ?: run {
                                            obfuscatedStart = accumulator
                                            accumulator
                                        }
                                        var obfuscatedRun = 0
                                        while (Formattings.OBFUSCATED.isActive(array.getOrNull(start + obfuscatedRun + 1) ?: ZERO)) {
                                            obfuscatedRun++
                                        }


                                        return@let it.withObfuscated(true).withHoverEvent(HoverEvent.ShowText(sequence.toComponent().substring(start - 1, start + obfuscatedRun - 1)))
                                    }
                                    obfuscatedStart = null

                                    it
                                }
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
        val (sink, component) = TextReplacements.toComponentCharSink()
        this.accept(sink)
        return component()
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
            for (format in Formattings.entries) {
                val until = format.matches(ch, previous)
                if (until != null) {
                    untilNext = until
                    formatting = format.mask
                    break
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
            } else {
                previous = ch
            }
        }
    }

    private fun requireDouble(char: Char): (Char, Char?) -> String? {
        val until = "$char$char"

        return { current, previous ->
            until.takeIf { current == char && previous == char }
        }
    }

    enum class Formattings(val matcher: (Char, Char?) -> String?) {
        BOLD(requireDouble('*')),
        UNDERLINE(requireDouble('_')),
        OBFUSCATED(requireDouble('|')),
        STRIKETHROUGH(requireDouble('~')),
        ITALIC({ _, previous ->
            if ((previous == '_' && ChatConfig.allowUnderscoreItalic) || previous == '*') {
                "$previous"
            } else null
        }),
        ;


        val mask = (1 shl ordinal).toByte()

        fun isActive(modifier: Byte) = (modifier and mask) == mask
        fun matches(current: Char, previous: Char?): String? = matcher(current, previous)
    }

}
