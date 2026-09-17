package me.owdding.skyocean.features.chat

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.utils.chat.ChatUtils
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.utils.regex.component.ComponentRegex
import tech.thatgravyboat.skyblockapi.utils.regex.component.match
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover

@Module
object SimpleBridgeFormatter {

    private val regularPrefix by lazy {
        Text.of("Discord > ") {
            color = KnownChatPrefix.DISCORD.color
            hover = ChatUtils.ADDED_BY_SKYOCEAN
        }
    }

    private val regex = ComponentRegex("(?<chatType>Guild|Officer) > (?:[^\\s:]+ )?(?<name>\\w{3,18})(?: \\[\\w+])?: (?<message>.+)")

    @Subscription
    fun onChatReceivedPost(event: ChatReceivedEvent.Post) {
        if (!ChatConfig.enableBridgeFormatter) return
        regex.match(event.component, "name", "message") { (name, message) ->
            if (name.stripped != ChatConfig.bridgeFormatterIgn) return@match
            if (ChatConfig.bridgeFormatterStylizedChatPrefix) {
                event.component = Text.join(
                    KnownChatPrefix.DISCORD.component,
                    " ",
                    message,
                )
            } else {
                event.component = Text.join(
                    regularPrefix,
                    message,
                )
            }
        }
    }

}
