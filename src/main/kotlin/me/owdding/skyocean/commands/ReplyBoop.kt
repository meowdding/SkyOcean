package me.owdding.skyocean.commands

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.find
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Module
object ReplyBoop {

    private var lastIgn: String? = null
    private var time = Instant.DISTANT_PAST
    private val regex = "^From (?!stash)(?:\\[.+] )?(?<author>[^:]*): (?<message>.*)".toRegex()

    private fun noUser() {
        Text.of("You haven't been messaged by anyone in the past 5 minutes!", OceanColors.WARNING).sendWithPrefix()
    }

    @Subscription
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        if (!ChatConfig.replyBoop) return
        event.registerWithCallback("rboop") {
            val ign = lastIgn ?: return@registerWithCallback noUser()
            if (time.since() > 5.minutes) return@registerWithCallback noUser()
            McClient.sendCommand("boop $ign")
        }
    }

    @Subscription
    fun onChatReceived(event: ChatReceivedEvent.Pre) {
        regex.find(event.text, "author") { (author) ->
            lastIgn = author
            time = currentInstant()
        }
    }

}
