package me.owdding.skyocean.features.chat

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.hypixel.SacksChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object SackNotification {

    @Subscription
    fun onSack(event: SacksChangeEvent) {
        if (!ChatConfig.enableSackNotification) return

        val amount = event.changedItems.filter { it.diff > 0 }.map {
            RepoItemsAPI.getItemName(it.item).stripped to it.diff
        }.filter { it.first in ChatConfig.sackNotificationItems }
        if (amount.isEmpty()) return

        Text.join(
            "Sack Notification",
            ChatUtils.SEPERATOR_COMPONENT,
            Text.join(amount.map { Text.of("${it.first} x${it.second}", OceanColors.HIGHLIGHT) }, separator = Text.of(", "))
        ) {
            color = OceanColors.BASE_TEXT
        }.sendWithPrefix()
    }

}
