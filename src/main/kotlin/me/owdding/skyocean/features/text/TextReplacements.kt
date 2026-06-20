package me.owdding.skyocean.features.text

import me.owdding.skyocean.features.text.MarkdownChat.toComponent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.FormattedCharSink
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent
import java.util.*
import java.util.function.Supplier
import kotlin.to

object TextReplacements {
    const val MINUS_ONE: Short = -1
    var array: Array<Short?> = Array(20) { null }

    fun createArray(length: Int): Array<Short?> {
        return Array(length) { null }
    }

    fun checkArray(length: Int): Array<Short?> {
        if (length > array.size) {
            array = createArray(length)
            return array
        }
        Arrays.fill(array, null)
        return array
    }

    @JvmStatic
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    fun wrapSink(formattedCharSink: FormattedCharSink): Pair<FormattedCharSink, Supplier<Boolean>> {
        val (sink, component) = toComponentCharSink()

        return sink to Supplier {
            val component = component()
            apply(component.visualOrderText).accept(formattedCharSink)
        }
    }


    fun toComponentCharSink(): Pair<FormattedCharSink, () -> MutableComponent> {
        val result = Text.of()
        var currentStyle: Style? = null
        var current = Text.of()
        var builder = StringBuilder()

        return FormattedCharSink { _, style, codepoint ->
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
        } to {
            current.append(builder.toString())
            current.style = currentStyle ?: Style.EMPTY
            result.append(current)
        }
    }

    @JvmStatic
    fun apply(instance: FormattedCharSequence): FormattedCharSequence = runCatching {
        if (McScreen.self is DisableReplacements) return@runCatching instance

        val builder = StringBuilder()
        instance.accept { _, _, codepoint ->
            builder.appendCodePoint(codepoint)
            true
        }
        val content = builder.toString()

        val arrayThingy = checkArray(content.length)

        val replacements = TextReplacementManager.replacements.filter { it.isEnabled() }

        replaceAll(arrayThingy, content, replacements)

        FormattedCharSequence { sink ->
            var accumulator = 0
            var position = 0
            instance.accept { _, originalStyle, codepoint ->
                val index = arrayThingy[accumulator]
                accumulator += codepoint.charLength
                index ?: return@accept sink.accept(position++, originalStyle, codepoint)

                if (index == MINUS_ONE) {
                    return@accept true
                }

                val replacement = replacements[index.toInt()].formattedValue

                replacement.accept { _, style, codepoint ->
                    sink.accept(position++, style.applyTo(originalStyle), codepoint)
                }

                true
            }
        }
    }.getOrElse { instance }

    val Int.charLength: Int
        get() {
            if (Character.isBmpCodePoint(this)) {
                return 1
            }

            return 2
        }

    fun replaceAll(array: Array<Short?>, content: String, replacements: List<TextReplacement>) {
        val length = content.length
        replacements.forEachIndexed { replacementIndex, textReplacement ->
            var start = 0
            val key = textReplacement.key
            val matchWholeWord = textReplacement.wholeWord

            while (start < length) {
                val index = content.indexOf(key, start, ignoreCase = textReplacement.ignoreCase)
                if (index == -1) break

                if (matchWholeWord && !content.isWholeWord(index, key.length)) {
                    start = index + 1
                    continue
                }

                start = index + 1

                if (array[index] != null) continue
                for (i in key.indices) {
                    array[index + i] = -1
                }
                array[index] = replacementIndex.toShort()
            }
        }
    }

    fun CharSequence.isWholeWord(index: Int, length: Int): Boolean {
        val before = if (index - 1 >= 0) this[index - 1] else null
        val after = if (index + length < this.length) this[index + length] else null

        return (before == null || before.isWhitespace()) && (after == null || after.isWhitespace())
    }

    @JvmStatic
    fun apply(content: String): String = apply(content.asComponent().visualOrderText).toComponent().string

}
