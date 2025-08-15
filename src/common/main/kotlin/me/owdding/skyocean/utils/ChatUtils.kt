package me.owdding.skyocean.utils

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.lib.rendering.text.TextShader
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.lib.rendering.text.textShader
import me.owdding.skyocean.config.CachedValue
import me.owdding.skyocean.config.Config
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration
import kotlin.time.Instant

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
    const val ITEM_IN_ITEM = "\u29c9"

}

internal object ChatUtils {
    const val ICON = Icons.WAVE

    const val ICON_WITH_SPACE = "$ICON "
    const val DARK_OCEAN_BLUE = OceanColors.DARK_CYAN_BLUE
    val ICON_COMPONENT = Text.of(ICON) { this.color = DARK_OCEAN_BLUE }
    val ICON_SPACE_COMPONENT = Text.of(ICON_WITH_SPACE) { this.color = DARK_OCEAN_BLUE }

    val prefixDelegate = CachedValue {
        Text.of {
            append("[")
            append("SkyOcean") {
                this.textShader = Config.prefixGradient.takeUnless { it.isDisabled }
            }
            append("] ")
            this.color = TextColor.GRAY
        }.withPotentialShadow()
    }
    val prefix: MutableComponent by prefixDelegate

    fun MutableComponent.withPotentialShadow(): MutableComponent {
        return if (Config.disableMessageTextShadow) {
            this.withoutShadow()
        } else {
            this
        }
    }

    fun asSkyOceanColor(text: String) = Text.of {
        append(text)
        this.color = OceanColors.SKYOCEAN_BLUE
    }

    fun asSkyOceanColorAnimated(text: String, useSelected: Boolean = false) = Text.of(text) {
        this.textShader = if (useSelected) Config.prefixGradient else OceanGradients.DEFAULT
    }

    fun MutableComponent.withoutShadow(): MutableComponent = this.apply {
        this.shadowColor = null
        this.siblings.filterIsInstance<MutableComponent>().forEach { it.withoutShadow() }
    }

    fun MutableComponent.append(init: MutableComponent.() -> Unit): MutableComponent = this.append(Text.of(init))

    fun chat(text: String, init: MutableComponent.() -> Unit = {}) = chat(Text.of(text, init))
    fun chat(text: Component) = Text.join(prefix, text).withPotentialShadow().send()
    fun chat(text: Component, id: String) = Text.join(prefix, text).withPotentialShadow().send(id)

    fun Component.sendWithPrefix() = chat(this)
    fun Component.sendWithPrefix(id: String) = chat(this, id)
}

object OceanColors {
    const val PINK = 0xf38ba8
    const val WARNING = PINK
    const val DARK_CYAN_BLUE = 0x355AA0
    const val SKYOCEAN_BLUE = 0x87CEEB
    const val LIGHT_GRAYISH_CYAN = 0xcff8ff
    const val BETTER_GOLD = 0xfc6f03
}

enum class OceanGradients(val colors: List<Int>) : TextShader by GradientTextShader(colors), Translatable {
    DEFAULT(0x87CEEB, 0x7FFFD4, 0x87CEEB),
    RAINBOW("#FF0000 #FF7F00 #FFFF00 #00FF00 #0000FF #4B0082 #8B00FF"),
    BISEXUAL("#D60270 #9B4F96 #0038A8"),
    GAY("#FF0000 #FF9900 #FFFF00 #33CC33 #3399FF #9900CC"),
    LESBIAN("#D62900 #FF9A56 #FFAC54 #FFFFFF #D362A4 #B9558A #A40061"),
    PANSEXUAL("#FF1B8D #FFD800 #1BB3FF"),
    ASEXUAL("#000000 #A4A4A4 #FFFFFF #810081"),
    NON_BINARY("#FFD800 #FFFFFF #9C59D1 #000000"),
    TRANS("#55CDFC #F7A8B8 #FFFFFF #F7A8B8 #55CDFC"),
    DISABLED(0),
    ;

    val isDisabled = this.colors.size == 1

    constructor(vararg colors: Int) : this(colors.toList())
    constructor(colors: String) : this(colors.split(Regex("\\s+")).map { it.removePrefix("#").toInt(16) }.toMutableList().apply { addLast(first()) })

    override fun getTranslationKey() = "skyocean.gradients.${name.lowercase()}"
}

data class ReplaceMessage(val message: Component) {
    private val stripped = message.stripped

    constructor(message: String) : this(Text.of(message))

    fun send() {
        message.sendWithPrefix(stripped)
    }
}

data class StaticMessageWithCooldown(val duration: Duration, val message: Component) {
    var lastSend: Instant = Instant.DISTANT_PAST

    fun send() {
        if (lastSend.since() < duration) return
        message.sendWithPrefix()
        lastSend = currentInstant()
    }
}

data class DynamicMessageCooldown(val duration: Duration) {
    var lastSend: Instant = Instant.DISTANT_PAST

    fun send(message: Component) {
        if (lastSend.since() < duration) return
        message.sendWithPrefix()
        lastSend = currentInstant()
    }
}
