package me.owdding.skyocean.features.mining.mineshaft

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MineshaftConfig
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyWidget
import tech.thatgravyboat.skyblockapi.api.events.info.TabWidget
import tech.thatgravyboat.skyblockapi.api.events.info.TabWidgetChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.MineshaftFoundEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toIntValue
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.anyMatch
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover

@Module
object PityMessage {

    private val regex = " Glacite Mineshafts: (?<current>[\\d,]+)/(?<max>[\\d,]+)".toRegex()
    private var lastPity = -1
    private var maxPity = 2000

    @Subscription
    @OnlyWidget(TabWidget.PITY)
    fun onWidget(event: TabWidgetChangeEvent) {
        if (!MineshaftConfig.mineshaftFoundPity) return
        regex.anyMatch(event.new, "current", "max") { (current, max) ->
            lastPity = current.toIntValue()
            maxPity = max.toIntValue()
        }
    }

    @Subscription
    fun onMineshaftFound(event: MineshaftFoundEvent) {
        if (!MineshaftConfig.mineshaftFoundPity) return
        McClient.runNextTick {
            Text.of("Mineshaft Pity | You found a mineshaft after $lastPity/$maxPity Pity!") {
                hover = Text.of("±10, Hypixel updates the Tablist every 3 seconds.").withColor(TextColor.PINK)
            }.sendWithPrefix()
        }
    }

}
