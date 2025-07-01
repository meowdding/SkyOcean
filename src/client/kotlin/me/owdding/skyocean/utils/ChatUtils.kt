package me.owdding.skyocean.utils

import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import kotlinx.datetime.Instant
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration

internal object ChatUtils {
    private const val gradient = "#87CEEB #7FFFD4"

    const val ICON = "\uD83C\uDF0A"
    const val ICON_WITH_SPACE = "$ICON "
    const val DARK_OCEAN_BLUE = 0x355AA0
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

    fun Component.sendWithPrefix() = chat(this)
}

data class StaticMessageWithCooldown(val duration: Duration, val message: Component) {
    fun send() {
        if (lastSend.since() < duration) return
        message.sendWithPrefix()
        lastSend = currentInstant()
    }

    companion object {
        var lastSend: Instant = Instant.DISTANT_PAST
    }
}

data class DynamicMessageCooldown(val duration: Duration) {
    fun send(message: Component) {
        if (lastSend.since() < duration) return
        message.sendWithPrefix()
        lastSend = currentInstant()
    }

    companion object {
        var lastSend: Instant = Instant.DISTANT_PAST
    }
}
