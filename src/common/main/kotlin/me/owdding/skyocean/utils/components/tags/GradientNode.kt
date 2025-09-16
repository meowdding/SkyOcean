package me.owdding.skyocean.utils.components.tags

import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.node.TextNode
import eu.pb4.placeholders.api.node.parent.ParentNode
import me.owdding.lib.rendering.text.builtin.GradientProvider
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.lib.rendering.text.withTextShader
import net.minecraft.network.chat.Style

class GradientNode(nodes: Array<TextNode>, val provider: GradientProvider) : ParentNode(*nodes) {
    override fun applyFormatting(style: Style, context: ParserContext): Style {
        return style.withTextShader(GradientTextShader(provider))
    }
}
