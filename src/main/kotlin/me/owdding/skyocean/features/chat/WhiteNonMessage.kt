package me.owdding.skyocean.features.chat

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.utils.RemoteStrings
import me.owdding.skyocean.utils.StringGroup.Companion.resolve
import net.minecraft.ChatFormatting
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.utils.regex.component.match
import tech.thatgravyboat.skyblockapi.utils.text.Text

@Module
object WhiteNonMessage {

    private val group = RemoteStrings.resolve()
    //                                                SBLVL            Emblem          Not Ranks
    private val regex by group.componentRegex("(?<prefix>(?:\\[\\d+]\\s)?(?:[^\\w\\s]\\s*)?)(?!\\[[^]]+]\\s)(?<username>\\w{3,16})(?<text>: .+)")

    @Subscription
    private fun ChatReceivedEvent.Post.onChat() {
        if (!ChatConfig.whiteNonMessage) return

        regex.match(component) { match ->
            component = Text.of {
                match["prefix"]?.let { append(it) }
                match["username"]?.let { append(it) }
                match["text"]?.let { append(it.copy().withStyle(ChatFormatting.WHITE)) }
            }
        }
    }

}
