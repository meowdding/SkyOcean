package me.owdding.skyocean.features.chat

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.chat.ChatConfig
import net.minecraft.ChatFormatting
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.utils.regex.component.ComponentRegex
import tech.thatgravyboat.skyblockapi.utils.regex.component.match
import tech.thatgravyboat.skyblockapi.utils.text.Text

@Module
object WhiteNonMessage {

    private val regex = ComponentRegex("(?<prefix>.* )?(?<username>.{1,16})(?<text>: .+)")

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
