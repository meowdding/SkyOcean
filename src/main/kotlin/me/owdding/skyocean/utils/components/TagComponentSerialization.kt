package me.owdding.skyocean.utils.components

import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import eu.pb4.placeholders.api.parsers.tag.TagRegistry
import eu.pb4.placeholders.api.parsers.tag.TextTag
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.lib.rendering.text.textShader
import me.owdding.skyocean.generated.EnumCodec
import me.owdding.skyocean.utils.chat.OceanGradients
import me.owdding.skyocean.utils.components.tags.GradientNode
import me.owdding.skyocean.utils.components.tags.OceanGradientNode
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import java.util.*
import kotlin.jvm.optionals.getOrNull

object TagComponentSerialization {

    val context: ParserContext = ParserContext.of()
    val tagParser: TagParser by lazy {
        val default = TagRegistry.DEFAULT
        val copies = listOf(
            listOf(
                "bold",
                "italic",
                "obfuscated",
                "underlined",
                "strikethrough",
                "color",
            ),

            ChatFormatting.entries.filter { it <= ChatFormatting.WHITE }.map {
                //~ if >= 26.2 'serializedName' -> 'name'
                it.name
            },
        ).flatten()
        val tagRegistry = TagRegistry.builder().apply {
            copies.forEach { default.getTag(it)?.let { tag -> this.add(tag) } }
            add(
                TextTag.enclosing("gradient", "gradient") { node, data, _ ->
                    val colors = mutableListOf<TextColor>()
                    val direction = data.get(
                        "dir",
                        EnumCodec.forKCodec(GradientTextShader.Direction.entries.toTypedArray()),
                    ).resultOrPartial().getOrNull() ?: GradientTextShader.Direction.RIGHT
                    val speed = data.get("speed", "0").toFloatOrNull() ?: 1f

                    while (true) {
                        val current = data.getNext("0") ?: break
                        val color = TextColor.parseColor(current).result().getOrNull() ?: continue
                        colors.add(color)
                    }

                    val colorList = colors.map { it.value }.toList()
                    GradientNode(node, direction, speed) { colorList }
                },
            )

            OceanGradients.entries.filterNot { it.isDisabled }.forEach {
                add(
                    TextTag.enclosing(it.name.lowercase(), "gradient") { node, data, _ ->
                        val direction = data.get(
                            "dir",
                            EnumCodec.forKCodec(GradientTextShader.Direction.entries.toTypedArray()),
                        ).resultOrPartial().getOrNull() ?: GradientTextShader.Direction.RIGHT
                        val speed = data.get("speed", "0").toFloatOrNull() ?: 1f
                        OceanGradientNode(node, it, direction, speed)
                    },
                )
            }
        }.build()
        TagParser.createQuickText(tagRegistry)
    }

    fun serialize(component: Component): String {
        val builder = StringBuilder()
        val styleStack = LinkedList<Style>()
        val openTags = LinkedList<Array<String>>()

        fun peek() = styleStack.peek() ?: Style.EMPTY
        fun pop() = if (styleStack.isNotEmpty()) styleStack.pop() else Style.EMPTY
        fun push(style: Style) = styleStack.push(style)

        fun popTags() = if (openTags.isNotEmpty()) openTags.pop() else emptyArray<String>()
        fun pushTags(vararg tags: String) = openTags.push(tags.toList().toTypedArray())
        fun applyCloseTags(tags: Iterable<String>) =
            tags.reversed().map { it.substringBefore(" ") }.forEach { builder.append("</$it>") }

        component.visualOrderText.accept { _, style, codepoint ->
            run {
                if (peek() == style) {
                    return@run
                }

                pop()
                val poppedTags = popTags().toList()
                if (peek() == style) {
                    applyCloseTags(poppedTags)
                    return@run
                } else {
                    val tags = style.toTags()
                    val duplicates = poppedTags.filter(tags::contains)
                    applyCloseTags(poppedTags.filterNot(duplicates::contains))
                    tags.filterNot(duplicates::contains).forEach { tag -> builder.append("<$tag>") }
                    pushTags(*tags.toTypedArray())
                    push(style)
                }
            }
            val chars = Character.toChars(codepoint)
            builder.append(chars)

            true
        }
        applyCloseTags(openTags.map { it.toList() }.flatten().distinct())

        return builder.toString()
    }

    private fun Style.toTags(): List<String> {
        val list = mutableListOf<String>()

        if (color != null) {
            val color = color!!.serialize()
            if (color.startsWith("#")) {
                list.add("color $color")
            } else {
                list.add("$color")
            }
        }
        if (this.textShader() != null) {
            when (val shader = this.textShader()) {
                is GradientTextShader if OceanGradients.entries.any {
                    it.colors.toIntArray().contentEquals(shader.gradientProvider.getColors().toIntArray())
                } -> {
                    val gradient = OceanGradients.entries.first { it.colors.toIntArray().contentEquals(shader.gradientProvider.getColors().toIntArray()) }
                    list.add(
                        buildString {
                            append(gradient.name.lowercase())
                            shader.serializeProperties().takeUnless { it.isEmpty() }?.let(::append)
                        },
                    )
                }

                is GradientTextShader -> list.add(
                    buildString {
                        append("gradient")

                        shader.serializeProperties().takeUnless { it.isEmpty() }?.let(::append)

                        for (i in shader.gradientProvider.getColors()) {
                            append(" #").append(i.toHexString().dropWhile { char -> char == '0' }.takeUnless { it.isEmpty() } ?: "0")
                        }
                    },
                )

                is OceanGradients -> list.add(shader.name.lowercase())
            }
        }

        if (isBold) list.add("bold")
        if (isItalic) list.add("italic")
        if (isObfuscated) list.add("obfuscated")
        if (isUnderlined) list.add("underlined")
        if (isStrikethrough) list.add("strikethrough")

        return list
    }

    fun GradientTextShader.serializeProperties() = buildString {
        if (direction != GradientTextShader.Direction.RIGHT) {
            append(" dir:").append(direction.name.lowercase())
        }
        if (speed != 1f) {
            append(" speed:").append(speed.toString().removeSuffix(".0"))
        }
    }

    fun deserialize(text: String): Component = tagParser.parseComponent(text, context)

}
