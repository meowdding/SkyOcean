package me.owdding.skyocean.utils.chat

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import kotlin.reflect.KProperty

class ComponentAnimator(val text: String, val colorA: Int, val colorB: Int, val speed: Long = 200L) {

    private val start = currentInstant()

    operator fun getValue(any: Any?, property: KProperty<*>): Component = get()
    fun get(): Component {
        val pauseLength = 3
        val waveLength = text.count { !it.isWhitespace() }
        val flashLength = 4
        val totalCycle = waveLength + pauseLength + flashLength + pauseLength

        val currentStep = (start.since().inWholeMilliseconds / speed).toInt() % totalCycle

        return Text.of {
            when {
                currentStep < waveLength -> wave(currentStep)
                currentStep < waveLength + pauseLength -> append(text, colorB)
                currentStep < waveLength + pauseLength + flashLength -> {
                    val flashIndex = currentStep - (waveLength + pauseLength)
                    append(text, if (flashIndex % 2 == 0) colorA else colorB)
                }
                else -> append(text, colorA)
            }
        }
    }

    private fun MutableComponent.wave(step: Int) {
        var index = 0
        for (char in text) {
            if (char.isWhitespace()) {
                append(char.toString())
                continue
            }
            if (index <= step) append(char.toString(), colorB)
            else append(char.toString(), colorA)
            index++
        }
    }
}
