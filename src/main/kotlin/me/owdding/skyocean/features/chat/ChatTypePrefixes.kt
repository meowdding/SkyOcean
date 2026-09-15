package me.owdding.skyocean.features.chat

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.chat.ChatConfig
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.regex.findWhen
import tech.thatgravyboat.skyblockapi.utils.text.SkyBlockColor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.font
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.substring

@Module
object ChatTypePrefixes {

    // The reason why the character used as the icon for the chat prefix is part of the actual prefix,
    // and why we replace the rest of the prefix with the same string but with an empty font, is so that
    // copying this chat message with other mods still shows the original chat prefix, instead of a
    // random Unicode character.
    val CHAT_PREFIXES = SkyOcean.id("chat_prefixes")
    val EMPTY_FONT = SkyOcean.id("empty_font")

    private val normalChatPrefixRegex = "^(?<type>[\\w-]+) > ".toRegex()
    private val npcRegex = "^\\[NPC] ".toRegex()

    @Subscription(priority = Subscription.LOW)
    fun onChat(event: ChatReceivedEvent.Post) {
        if (!ChatConfig.chatPrefixesIcons) return

        findWhen(event.text) {
            case(normalChatPrefixRegex) {
                val chat = ConfigChatPrefix.fromChat(it["type"]!!) ?: return@case
                if (!shouldReplace(chat)) return@case
                val stringLength = it.string.length

                event.component = Text.of {
                    // The first character of the chat prefix
                    append(chat.icon) {
                        withStyle(event.component.substring(0, 1).style)
                        font = CHAT_PREFIXES
                        color = TextColor.WHITE
                    }
                    // The rest of what got matched inside the regex
                    append(event.component.substring(1, stringLength)) {
                        font = EMPTY_FONT
                    }
                    append(event.component.substring(stringLength))
                }
            }

            case(npcRegex) {
                if (!shouldReplace(NPC)) return@case
                val stringLength = it.string.length
                event.component = Text.of {
                    // The first '['
                    append(event.component.substring(0, 1)) {
                        font = EMPTY_FONT
                    }
                    // Specifically the 'N'
                    append(ConfigChatPrefix.NPC.icon) {
                        withStyle(event.component.substring(1, 2).style)
                        font = CHAT_PREFIXES
                        color = TextColor.WHITE
                    }
                    // Everything after the N, so 'PC]'
                    append(event.component.substring(2, stringLength)) {
                        font = EMPTY_FONT
                    }
                    append(event.component.substring(stringLength))
                }
            }
        }
    }

    private fun shouldReplace(prefix: ConfigChatPrefix): Boolean {
        return prefix in ChatConfig.allowedChatPrefixesIcons
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

    val icon: String get() = delegate.icon
    override fun toString() = delegate.toString()

    companion object {
        val default = entries

        val searchTerms = entries.map(ConfigChatPrefix::toString)
        fun fromChat(name: String): ConfigChatPrefix? = entries.find {
            val delegate = it.delegate
            delegate.isRealChat && delegate.chatName == name
        }
    }
}

enum class KnownChatPrefix(
    val color: Int,
    chatName: String? = null,
    displayName: String? = null,
    val isRealChat: Boolean = true,
) {
    PARTY(SkyBlockColor.BLUE),
    GUILD(SkyBlockColor.DARK_GREEN),
    OFFICER(0x007700),
    FRIEND(0x33CC33),
    SKYBLOCK_COOP(SkyBlockColor.DARK_AQUA, chatName = "Co-op", displayName = "CO-OP"),

    NPC(SkyBlockColor.GOLD, isRealChat = false),
    // Even tho skyocean doesn't use this, it makes it so other mods can use it :3
    DISCORD(0x5865f2, isRealChat = false),
    ;

    val displayName: String = displayName ?: name
    val icon: String = this.displayName.first().toString()
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
