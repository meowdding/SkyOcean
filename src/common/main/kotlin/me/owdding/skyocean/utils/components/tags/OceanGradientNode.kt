package me.owdding.skyocean.utils.components.tags

import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.node.TextNode
import eu.pb4.placeholders.api.node.parent.ParentNode
import me.owdding.lib.rendering.text.withTextShader
import me.owdding.skyocean.utils.chat.OceanGradients
import net.minecraft.network.chat.Style

class OceanGradientNode(children: Array<out TextNode>, val gradient: OceanGradients) : ParentNode(*children) {

    override fun applyFormatting(style: Style, context: ParserContext): Style {
        return style.withTextShader(gradient)
    }
}
