package me.owdding.skyocean.utils.extensions

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.lib.rendering.text.textShader
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.chat.OceanGradients
import net.minecraft.ChatFormatting
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined
import kotlin.collections.chunked

fun Display.withTextFormattingInfo() = withTooltip {
    add("The text field below supports a some formatting tags!")
    space()
    add("The basic formatting tags include the following")
    add {
        append(" • ")
        append("<bold>") { this.bold = true }
        append(", ")
        append("<italic>") { this.italic = true }
        append(", ")
        append("<strikethrough>") { this.strikethrough = true }
        append(", ")
        append("<underlined>") { this.underlined = true }
        append(" and <obfuscated>")
    }
    ChatFormatting.entries.filter { it < ChatFormatting.OBFUSCATED }.map {
        //? >= 26.2
        val value = net.minecraft.network.chat.TextColor.fromLegacyFormat(it)!!

        //~ if >= 26.2 'it.serializedName' -> 'value.serialize()'
        val name = value.serialize()
        //~ if >= 26.2 'it.color!!' -> 'value.value'
        val color = value.value

        text("<${name}>") {
            this.color =color
        }
    }.chunked(5).forEach {
        add {
            append(" • ")
            append(Text.join(it, separator = text(", ")))
        }
    }
    add(" • ") {
        append("<color #f38ba8>") {
            this.color = OceanColors.PINK
        }
    }
    space()
    add("The \"complex\" style tags include the following")
    OceanGradients.entries.filterNot { it.isDisabled }.map {
        text("<${it.name.lowercase()}>") {
            this.textShader = it
        }
    }.chunked(5).forEach {
        add {
            append(" • ")
            append(Text.join(it, separator = text(", ")))
        }
    }
    add {
        append(" • ")
        append("<gradient ")
        append("#color1 ") { this.color = TextColor.BLUE }
        append("#color2 ") { this.color = TextColor.GREEN }
        append("... ") { this.color = TextColor.GRAY }
        append("#colorN ") { this.color = TextColor.MAGENTA }
        append("#color1") { this.color = TextColor.BLUE }
        append(">")
    }
    space()
    add("You can also customize the direction and speed of the gradient.")
    add("<") {
        color = CatppuccinColors.Mocha.overlay0
        append("trans ", CatppuccinColors.Mocha.green)
        append("dir", CatppuccinColors.Mocha.yellow)
        append(":<[")
        append(GradientTextShader.Direction.entries.joinToComponent(Text.of("|", CatppuccinColors.Mocha.overlay0)) {
            Text.of(it.toString().lowercase(), CatppuccinColors.Mocha.text)
        })
        append("]>")
        append(" speed", CatppuccinColors.Mocha.yellow)
        append(":")
        append("<number>", CatppuccinColors.Mocha.lavender)
        append(" ...>")
    }
    add("This also works for the generic gradient.")
    space()
    add("Note! To get a gradient that loops perfectly you\n must include the start color at the end again!") {
        this.color = TextColor.YELLOW
    }
}
