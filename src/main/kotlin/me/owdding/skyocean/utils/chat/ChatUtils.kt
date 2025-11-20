package me.owdding.skyocean.utils.chat

import com.mojang.serialization.MapCodec
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.lib.events.RegisterTextShaderEvent
import me.owdding.lib.rendering.text.TextShader
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.lib.rendering.text.textShader
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.config.CachedValue
import me.owdding.skyocean.config.Config
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.PreInitModule
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.font
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration
import kotlin.time.Instant

enum class ComponentIcon(val image: String) {
    WARDROBE("wardrobe"),
    ACCESSORIES("accessory"),
    FORGE("anvil"),
    CHESTS("chest"),
    ITEM_IN_ITEM("item_in_item"),
    BOX("box"),
    ;

    val icon = Character.toChars(0xE000 + (ordinal + 1)).first()
    val text = text(icon.toString()) {
        this.font = ComponentIcons.ID
    }

}

object ComponentIcons {
    val WAVE = text(Icons.WAVE) { this.color = OceanColors.DARK_CYAN_BLUE }
    val CHECKMARK = text(Icons.CHECKMARK)
    val CROSS = text(Icons.CROSS)
    val WARNING = text(Icons.WARNING)
    val HOLLOW_FLAG = text(Icons.HOLLOW_FLAG)
    val FILLED_FLAG = text(Icons.FILLED_FLAG)

    val ID = SkyOcean.id("font_icons")

    val WARDROBE = ComponentIcon.WARDROBE.text
    val ACCESSORIES = ComponentIcon.ACCESSORIES.text
    val FORGE = ComponentIcon.FORGE.text
    val CHESTS = ComponentIcon.CHESTS.text
    val ITEM_IN_ITEM = ComponentIcon.ITEM_IN_ITEM.text
    val BOX = ComponentIcon.BOX.text
}

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
    const val SPACE_WITH_ICON = " $ICON"
    const val DARK_OCEAN_BLUE = OceanColors.DARK_CYAN_BLUE
    val ICON_COMPONENT = Text.of(ICON) { this.color = DARK_OCEAN_BLUE }
    val ICON_SPACE_COMPONENT = Text.of(ICON_WITH_SPACE) { this.color = DARK_OCEAN_BLUE }
    val SPACE_ICON_COMPONENT = Text.of(SPACE_WITH_ICON) { this.color = DARK_OCEAN_BLUE }
    val SEPERATOR_COMPONENT = Text.of(" | ", OceanColors.SEPARATOR)

    val prefixDelegate = CachedValue {
        Text.of {
            append("[")
            append("SkyOcean") {
                this.textShader = Config.prefixGradient.takeUnless { it.isDisabled }
                if (Config.clickablePrefix) {
                    this.onClick { McClient.setScreenAsync { ResourcefulConfigScreen.getFactory("skyocean").apply(null) } }
                    this.hover = Text.of("Click to open SkyOcean's config!").withColor(TextColor.GRAY)
                }
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

    const val BASE_TEXT = 0xcdd6f4
    const val SEPARATOR = 0x585b70
    const val HIGHLIGHT = 0xcba6f7
}

enum class OceanGradients(val colors: List<Int>, private val shader: GradientTextShader = GradientTextShader(colors)) : TextShader by shader, Translatable {
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

    override val id: ResourceLocation = id("named_gradient")
    val isDisabled = this.colors.size == 1

    constructor(vararg colors: Int) : this(colors.toList())
    constructor(colors: String) : this(colors.split(Regex("\\s+")).map { it.removePrefix("#").toInt(16) }.toMutableList().apply { addLast(first()) })

    override fun getTranslationKey() = "skyocean.gradients.${name.lowercase()}"

    @PreInitModule
    companion object {
        val ID = id("named_gradient")
        val CODEC: MapCodec<OceanGradients> = SkyOceanCodecs.getCodec<OceanGradients>().fieldOf("name")

        @Subscription
        fun registerShaders(event: RegisterTextShaderEvent) {
            event.register(ID, CODEC)
        }
    }
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
