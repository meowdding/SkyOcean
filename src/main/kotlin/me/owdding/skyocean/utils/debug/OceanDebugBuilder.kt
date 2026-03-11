package me.owdding.skyocean.utils.debug

import me.owdding.skyocean.utils.Utils.addAll
import me.owdding.skyocean.utils.Utils.componentList
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.chat.ChatUtils
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.prefix
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.clipboard
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import tech.thatgravyboat.skyblockapi.api.events.misc.DebugBuilder as ApiDebugBuilder

sealed interface DebugEntry {
    fun asComponents(depth: Int): List<Component>
    val entries: List<DebugEntry>
    val copyValue: String
}

data class FieldEntry(
    val component: List<Component>,
    override val copyValue: String,
) : DebugEntry {
    constructor(component: Component, copyValue: String) : this(listOf(component), copyValue)

    override val entries: List<DebugEntry> = listOf(this)
    override fun asComponents(depth: Int): List<Component> = component
}

open class DebugBuilder : ApiDebugBuilder(CommonComponents.EMPTY, CommonComponents.EMPTY), DebugEntry {
    override val entries = mutableListOf<DebugEntry>()

    open fun addEntry(entry: DebugEntry) {
        entries.add(entry)
    }

    open fun <T> iterable(field: String, iterable: Iterable<T>, converter: DebugBuilder.(T) -> Unit) {
        iterable(!field, iterable, converter)
    }

    open fun <T> iterable(field: Component, iterable: Iterable<T>, converter: DebugBuilder.(T) -> Unit) {
        entries.add(
            ListDebugBuilder(field).apply {
                iterable.forEach {
                    addEntry(
                        DebugBuilder().apply {
                            converter(it)
                        },
                    )
                }
            },
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun <Type> field(property: KProperty0<Type>, description: Component? = null, copyValue: String? = null) {
        field(property.name, property.get(), description, copyValue)
    }

    @Suppress("NOTHING_TO_INLINE")
    context(receiver: Receiver)
    inline fun <Type, Receiver> field(property: KProperty1<Receiver, Type>, description: Component? = null, copyValue: String? = null) {
        field(property.name, property.get(receiver), description, copyValue)
    }

    fun literal(name: String, description: Component? = null, copyValue: String = name) {
        literal(!name, description, copyValue)
    }

    fun literal(name: Component, description: Component? = null, copyValue: String = name.stripped) {
        entries.add(
            FieldEntry(
                Text.of {
                    append(name)
                    if (description != null) {
                        hover = description
                    }
                    clipboard = copyValue
                },
                copyValue
            ),
        )
    }

    override fun <T> field(field: String, value: T?, description: Component?, copyValue: String?) {
        val copyValue = copyValue ?: value.toString()
        entries.add(
            FieldEntry(
                Text.of {
                    append(field)
                    append(": ")
                    append(format(value))

                    if (description != null) {
                        hover = description
                    }

                    clipboard = copyValue
                },
                "$field: $copyValue",
            ),
        )
    }

    override fun <T> format(value: T?): Component {
        when (value) {
            is Number -> Text.of(value.toFormattedString()) {
                color = TextColor.AQUA
                append(
                    when (value) {
                        is Double -> "d"
                        is Long -> "L"
                        is Float -> "f"
                        is Short -> "s"
                        is Byte -> "b"
                        is Int -> ""
                        else -> " - ${value.javaClass.simpleName}"
                    },
                )
            }
        }
        return super.format(value)
    }

    override fun asComponents(depth: Int): List<Component> = entries.flatMap { it.asComponents(depth + 1) }.map { it.prefix("  ") }
    override fun build(): Component = Text.multiline(asComponents(0))

    override val copyValue: String get() = entries.joinToString("\n") { "  " + it.copyValue }
}


val depthColor = listOf(
    CatppuccinColors.Mocha.blue,
    CatppuccinColors.Mocha.pink,
    CatppuccinColors.Mocha.red,
    CatppuccinColors.Mocha.green,
    CatppuccinColors.Mocha.mauve,
    CatppuccinColors.Mocha.yellow,
)

class ListDebugBuilder(val field: Component) : DebugBuilder() {
    override fun asComponents(depth: Int): List<Component> = componentList {
        val parts = entries.map { it.entries.map { it.asComponents(depth + 1) }.flatten() }

        add {
            append(field)
            append(" (")
            append(parts.size.toString(), CatppuccinColors.Mocha.lavender)
            append(")")
        }
        parts.forEach { components ->
            components.forEachIndexed { index, line ->
                add {
                    if (index != 0) {
                        append(" ")
                    } else {
                        append("- ") { color = depthColor[depth % depthColor.size] }
                    }
                    append(line)
                }
            }
        }
    }

    override val copyValue: String
        get() = buildString {
            val parts = entries.map { it.entries.map { it.copyValue } }

            append(this@ListDebugBuilder.field.stripped).append(" (${parts.size})").appendLine(":")
            append(
                parts.joinToString("\n") { values ->
                    values.mapIndexed { index, line ->
                        buildString {
                            if (index == 0) {
                                append("- ")
                            } else {
                                append("  ")
                            }
                            append(line)
                        }
                    }.joinToString("\n")
                },
            )
        }
}

data class RootDebugBuilder(val displayName: Component) : DebugBuilder() {

    override fun asComponents(depth: Int): List<Component> = componentList {
        val lines = entries.flatMap { it.asComponents(depth + 1) }
        add {
            append(ChatUtils.prefix)
            append(displayName) {
                color = CatppuccinColors.Mocha.green
            }
            append(" <copy>") {
                color = CatppuccinColors.Mocha.blue
                this.clipboard = copyValue
            }
        }
        addAll(lines)
    }

    override val copyValue: String
        get() = buildString {
            appendLine("```")
            append(displayName.stripped)
            append("\n")
            appendLine(super.entries.joinToString("\n") { it.copyValue })
            append("```")
        }
}
