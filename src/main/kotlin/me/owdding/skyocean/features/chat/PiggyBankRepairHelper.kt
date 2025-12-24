package me.owdding.skyocean.features.chat

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.command

@Module
object PiggyBankRepairHelper {

    private val regex = "You died(?: and your piggy bank cracked!|, lost [\\d.,]* coins and your piggy bank broke!)".toRegex()

    @Subscription
    fun onChat(event: ChatReceivedEvent.Pre) {
        if (!ChatConfig.piggyRepairHelper) return
        if (!regex.matches(event.text)) return

        McClient.runNextTick {
            Text.of("Click here get 8 Pork from your sacks.") {
                command = "/gfs ENCHANTED_PORK 8"
            }.sendWithPrefix()
        }
    }
}
