package codes.cookies.skyocean.utils

import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.style

internal object ChatUtils {

    val prefix = TagParser.QUICK_TEXT_SAFE.parseText("<gray>[<gr #87CEEB #7FFFD4>SkyOcean</gr>]</gray>", ParserContext.of()).copy().withoutShadow()

    fun MutableComponent.withoutShadow() = this.apply {
        this.shadowColor = null
    }

    var MutableComponent.shadowColor: Int?
        get() = style.shadowColor
        set(value) {
            this.style { this.withShadowColor(value ?: 0) }
        }
}
