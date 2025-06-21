package me.owdding.skyocean.utils

import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor

internal object ChatUtils {
    private const val gradient = "#87CEEB #7FFFD4"

    const val ICON = "\uD83C\uDF0A"
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

    fun chat(text: String, init: MutableComponent.() -> Unit = {}) = chat(Text.of(text, init))
    fun chat(text: Component) = Text.join(prefix, text).withoutShadow().send()

    fun MutableComponent.sendWithPrefix() = chat(this)
}

object OceanColors {
    const val PINK = 0xf38ba8
    const val WARNING = PINK
    const val DARK_CYAN_BLUE = 0x355AA0
    const val LIGHT_GRAYISH_CYAN = 0xcff8ff
}
