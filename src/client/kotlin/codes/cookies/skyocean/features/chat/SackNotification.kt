package codes.cookies.skyocean.features.chat

import codes.cookies.skyocean.config.features.chat.ChatConfig
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.ChatUtils
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.hypixel.SacksChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object SackNotification {

    @Subscription
    fun onSack(event: SacksChangeEvent) {
        if (!ChatConfig.enableSackNotification) return

        val amount = event.changedItems.filter { it.diff > 0 }.map {
            RepoItemsAPI.getItemName(it.item).stripped to it.diff
        }.filter { it.first in ChatConfig.sackNotificationItems }
        if (amount.isEmpty()) return

        val text = Text.join(
            "Sack Notification | ",
            amount.joinToString(", ") { "${it.first} x${it.second}" },
        )

        ChatUtils.chat(text)
    }

}
