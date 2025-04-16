package codes.cookies.skyocean.helpers

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text

class TooltipBuilder() {

    constructor(lines: List<Any>) : this() {
        this.lines.addAll(lines)
    }

    private val lines = mutableListOf<Any>()

    fun add(line: Component) = lines.add(line)

    fun space() = lines.add(CommonText.EMPTY)

    fun add(number: Number, init: MutableComponent.() -> Unit = {}) = lines.add(Text.of(number.toString(), init))
    fun add(boolean: Boolean, init: MutableComponent.() -> Unit = {}) = lines.add(Text.of(boolean.toString(), init))
    fun add(text: String, init: MutableComponent.() -> Unit = {}) = lines.add(Text.of(text, init))
    fun add(init: MutableComponent.() -> Unit) = lines.add(Text.of("", init))

    fun isEmpty() = lines.isEmpty()
    fun build(): Component = Text.multiline(*lines.toTypedArray())
    fun lines() = lines
}
