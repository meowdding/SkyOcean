package me.owdding.skyocean.features.text

import net.minecraft.util.FormattedCharSequence
import tech.thatgravyboat.skyblockapi.utils.text.Text

object TextReplacements {
    const val MINUS_ONE: Short = -1

    val replacements: MutableList<Pair<String, FormattedCharSequence>> = ArrayList()

    init {
        replacements.add("meowo" to Text.of("mrow").visualOrderText)
    }

    @JvmStatic
    fun apply(instance: FormattedCharSequence): FormattedCharSequence = runCatching {
        val builder = StringBuilder()
        instance.accept { _, _, codepoint ->
            builder.appendCodePoint(codepoint)
            true
        }
        val content = builder.toString()

        val arrayThingy = Array<Short?>(content.length) { null }

        replaceAll(arrayThingy, content)

        FormattedCharSequence { sink ->
            var accumulator = 0
            var position = 0
            instance.accept { _, style, codepoint ->
                val index = arrayThingy[accumulator++] ?: return@accept sink.accept(position++, style, codepoint)

                if (index == MINUS_ONE) {
                    return@accept true
                }

                val (_, replacement) = replacements[index.toInt()]

                replacement.accept { _, style, codepoint ->
                    sink.accept(position++, style, codepoint)
                }

                true
            }
        }
    }.getOrElse { instance}


    fun replaceAll(array: Array<Short?>, content: String) {
        val length = content.length
        replacements.forEachIndexed { replacementIndex, pair ->
            var start = 0
            val (key) = pair

            while (start < length) {
                val index = content.indexOf(key, start)
                if (index == -1) break

                start = index + 1

                if (array[index] != null) continue
                for (i in key.indices) {
                    array[index + i] = -1
                }
                array[index] = replacementIndex.toShort()
            }
        }
    }

}
