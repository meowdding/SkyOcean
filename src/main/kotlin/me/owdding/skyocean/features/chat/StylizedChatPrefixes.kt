package me.owdding.skyocean.features.chat

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.utils.chat.ChatUtils
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.regex.findWhen
import tech.thatgravyboat.skyblockapi.utils.text.SkyBlockColor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.font
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.substring

@Module
object StylizedChatPrefixes {

    // The reason why the character used as the icon for the chat prefix is part of the actual prefix,
    // and why we replace the rest of the prefix with the same string but with an empty font, is so that
    // copying this chat message with other mods still shows the original chat prefix, instead of a
    // random Unicode character.
    val STYLIZED_CHAT_PREFIXES = SkyOcean.id("stylized_chat_prefixes")
    val EMPTY_FONT = SkyOcean.id("empty_font")

    private val normalChatPrefixRegex = "^(?<type>[\\w-]+) >".toRegex()
    private val npcRegex = "^\\[NPC]".toRegex()

    @Subscription(priority = Subscription.LOW)
    fun onChat(event: ChatReceivedEvent.Post) {
        if (!ChatConfig.enableStylizedChatPrefixes) return

        findWhen(event.text) {
            case(normalChatPrefixRegex) { match ->
                val chat = ConfigChatPrefix.fromChat(match["type"]!!) ?: return@case
                if (!shouldReplace(chat)) return@case

                event.component = Text.of {
                    append(chat.component)
                    append(event.component.substring(match.string.length))
                }
            }

            case(npcRegex) { match ->
                if (!shouldReplace(NPC)) return@case

                event.component = Text.of {
                    append(ConfigChatPrefix.NPC.component)
                    append(event.component.substring(match.string.length))
                }
            }
        }
    }

    private fun shouldReplace(prefix: ConfigChatPrefix): Boolean {
        return prefix in ChatConfig.allowedStylizedChatPrefixes
    }

}

enum class ConfigChatPrefix(private val delegate: KnownChatPrefix) {
    PARTY(KnownChatPrefix.PARTY),
    GUILD(KnownChatPrefix.GUILD),
    OFFICER(KnownChatPrefix.OFFICER),
    FRIEND(KnownChatPrefix.FRIEND),
    SKYBLOCK_COOP(KnownChatPrefix.SKYBLOCK_COOP),

    NPC(KnownChatPrefix.NPC),
    ;

    val component: Component get() = delegate.component
    override fun toString() = delegate.displayName

    companion object {
        val default = entries

        val searchTerms = entries.map(ConfigChatPrefix::toString)
        fun fromChat(name: String): ConfigChatPrefix? = entries.find {
            val delegate = it.delegate
            delegate.isRegularChat && delegate.chatName == name
        }
    }
}

enum class KnownChatPrefix(
    val color: Int,
    chatName: String? = null,
    displayName: String? = null,
    val isRegularChat: Boolean = true,
) {
    PARTY(SkyBlockColor.BLUE),
    GUILD(SkyBlockColor.DARK_GREEN),
    OFFICER(0x007700),
    FRIEND(0x33CC33),
    SKYBLOCK_COOP(SkyBlockColor.DARK_AQUA, chatName = "Co-op", displayName = "CO-OP"),

    DISCORD(0x5865f2, isRegularChat = false),
    NPC(SkyBlockColor.GOLD, isRegularChat = false) {
        override val component: Component = Text.join(
            "[",
            Text.of(icon) {
                font = StylizedChatPrefixes.STYLIZED_CHAT_PREFIXES
                hover = ChatUtils.ADDED_BY_SKYOCEAN
            },
            displayName.drop(1),
            "]",
        ) {
            font = StylizedChatPrefixes.EMPTY_FONT
        }
    },
    ;

    val displayName: String = displayName ?: name
    val icon: String = this.displayName.first().toString()

    open val component: Component = Text.join(
        Text.of(icon) {
            font = StylizedChatPrefixes.STYLIZED_CHAT_PREFIXES
            hover = ChatUtils.ADDED_BY_SKYOCEAN
        },
        this.displayName.drop(1),
        " >",
    ) {
        font = StylizedChatPrefixes.EMPTY_FONT
    }

    val chatName: String = chatName ?: toFormattedName()

    override fun toString() = displayName

    companion object {
        init {
            if (McClient.isDev) {
                val icons = entries.map { it.icon }
                val set = icons.toSet()
                val diff = icons.size - set.size
                require(diff == 0) { "There are $diff KnownChatPrefixes using the same icon" }
            }
        }
    }

}
