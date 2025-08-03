package me.owdding.skyocean.utils

import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor

internal object Icons {

    const val WAVE = "\uD83C\uDF0A"

    const val CHECKMARK = "\u2714"
    const val CROSS = "\u274C"
    const val WARNING = "\u26A0"
    const val HOLLOW_FLAG = "\u2690"
    const val FILLED_FLAG = "\u2691"

    const val WARDROBE = "\u2602"
    const val VAULT = "\u00a5"
    const val ACCESSORIES = "\u16f0"
    const val FORGE = "\u16dd"
    const val CHESTS = "\u2302"
    const val RIFT = "\u0444"

}

internal object ChatUtils {
    private const val gradient = "#87CEEB #7FFFD4"

    const val ICON = Icons.WAVE

    const val ICON_WITH_SPACE = "$ICON "
    const val DARK_OCEAN_BLUE = OceanColors.DARK_CYAN_BLUE
    val ICON_COMPONENT = Text.of(ICON) { this.color = DARK_OCEAN_BLUE }
    val ICON_SPACE_COMPONENT = Text.of(ICON_WITH_SPACE) { this.color = DARK_OCEAN_BLUE }

    val prefix = TagParser.QUICK_TEXT_SAFE.parseText("<gray>[<gr $gradient>SkyOcean</gr>]</gray> ", ParserContext.of()).copy().withoutShadow()

    const val BETTER_GOLD = 0xfc6f03

    fun asSkyOceanColor(text: String) = TagParser.QUICK_TEXT_SAFE.parseText("<gr $gradient>$text</gr>", ParserContext.of()).copy().withoutShadow()

    fun MutableComponent.withoutShadow(): MutableComponent = this.apply {
        this.shadowColor = null
        this.siblings.filterIsInstance<MutableComponent>().forEach { it.withoutShadow() }
    }

    fun MutableComponent.append(init: MutableComponent.() -> Unit) = this.append(Text.of(init))

    fun chat(text: String, init: MutableComponent.() -> Unit = {}) = chat(Text.of(text, init))
    fun chat(text: Component) = Text.join(prefix, text).withoutShadow().send()

    fun Component.sendWithPrefix() = chat(this)
}

object OceanColors {
    const val PINK = 0xf38ba8
    const val WARNING = PINK
    const val DARK_CYAN_BLUE = 0x355AA0
    const val LIGHT_GRAYISH_CYAN = 0xcff8ff
}
