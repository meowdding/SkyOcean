package me.owdding.skyocean.features.chat

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.chat.ChatConfig
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent

@Module
object BlazetekkHider {
    private val messages = setOf(
        "Your radio is weak. Find another enjoyer to boost it.",
        "Your radio signal is strong!",
        "Your radio lost signal. There's too many enjoyers on this channel.",
    )

    @Subscription
    @OnlyOnSkyBlock
    context(event: ChatReceivedEvent.Pre)
    fun onChat() {
        if (!ChatConfig.hideBlazetekkMessages) return
        if (event.text in messages) {
            event.cancel()
        }
    }

}
