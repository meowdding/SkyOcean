package codes.cookies.skyocean.utils

import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor

internal object ChatUtils {
    private const val gradient = "#87CEEB #7FFFD4"

    val prefix = TagParser.QUICK_TEXT_SAFE.parseText("<gray>[<gr $gradient>SkyOcean</gr>]</gray> ", ParserContext.of()).copy().withoutShadow()

    fun asSkyOceanColor(text: String) = TagParser.QUICK_TEXT_SAFE.parseText("<gr $gradient>$text</gr>", ParserContext.of()).copy().withoutShadow()

    fun MutableComponent.withoutShadow(): MutableComponent = this.apply {
        this.shadowColor = null
        this.siblings.filterIsInstance<MutableComponent>().forEach { it.withoutShadow() }
    }

    fun chat(text: String, init: MutableComponent.() -> Unit = {}) = chat(Text.of(text, init))
    fun chat(text: Component) = Text.join(prefix, text).withoutShadow().send()
}
