package me.owdding.skyocean.features.dev

import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.debugToggle
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ItemDebugTooltipEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Module
object IdDebug {

    val enable by debugToggle("item/id_debug", "Shows the skyocean item id in the tooltip")

    @OptIn(ExperimentalContracts::class)
    private inline fun <T, R> T.ifEnabled(block: T.() -> R) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        if (!enable) return
        block()
    }

    @Subscription
    fun debug(event: ItemDebugTooltipEvent) = ifEnabled {
        event.add(
            Text.of {
                append("SkyBlockId: ")
                append(event.item.getSkyBlockId()?.id ?: "null")
                this.color = TextColor.GRAY
            },
        )
    }

}
