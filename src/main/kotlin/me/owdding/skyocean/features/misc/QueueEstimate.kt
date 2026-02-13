package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.displays.centerIn
import me.owdding.lib.extensions.toReadableString
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.utils.chat.ChatUtils
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.render.RenderHudEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Module
object QueueEstimate {

    private val history = mutableListOf<Pair<Instant, Int>>()
    private var estimate: Instant? = null
    private var lastTitle: String? = null
    private val regex = Regex("You are #(?<position>\\d+) in the queue!")

    @Subscription
    fun onTick(event: TickEvent) {
        if (!MiscConfig.queueEstimation) return
        val title = McClient.self.gui.subtitle?.stripped ?: run {
            lastTitle = null
            history.clear()
            estimate = null
            return
        }
        if (title != lastTitle) {
            lastTitle = title
            val matches = regex.match(title, "position") { (position) ->
                history.add(currentInstant() to position.toInt())
                estimate = predict()
            }
            if (!matches) {
                history.clear()
                estimate = null
            }
        }
    }

    @Subscription
    fun onRender(event: RenderHudEvent) {
        if (!MiscConfig.queueEstimation) return
        if (history.isEmpty()) return
        val title = Text.of {
            append(ChatUtils.ICON_SPACE_COMPONENT)
            append("Queue Estimate") { color = TextColor.PINK }
        }
        val display = estimate?.let {
            DisplayFactory.vertical {
                string(title)
                string("Estimate: ") {
                    color = TextColor.GRAY
                    append(it.toJavaInstant().toReadableString()) {
                        color = TextColor.PINK
                    }
                }
                string("Time Left: ") {
                    color = TextColor.GRAY
                    val duration = it - currentInstant()
                    append(duration.toReadableTime()) {
                        color = TextColor.PINK
                    }
                }

                if (history.size >= 5) return@vertical
                string("Less than 5 data points, estimate may be inaccurate.") {
                    color = TextColor.RED
                }
            }
        } ?: DisplayFactory.vertical {
            string(title)
            string("Collecting data...") {
                color = TextColor.GRAY
            }
        }

        display.centerIn(McClient.window.guiScaledWidth, McClient.window.guiScaledHeight / 3).render(event.graphics)
    }

    private fun predict(): Instant? {
        if (history.size < 2) return null

        val startTime = history.first().first

        val points = history.map { (time, position) -> (time - startTime).inWholeSeconds to position.toDouble() }

        val averageX = points.map { it.first }.average()
        val averageY = points.map { it.second }.average()

        var above = 0.0
        var below = 0.0
        // m = SUM((xi - averageX) * (yi - averageY)) / SUM((xi - averageX)^2)
        for ((x, y) in points) {
            above += (x - averageX) * (y - averageY)
            below += (x - averageX) * (x - averageX)
        }

        if (below == 0.0) return null
        val slope = above / below

        if (slope >= 0) return null

        val secondsToZero = -(averageY - (slope * averageX)) / slope

        return startTime.plus(secondsToZero.seconds)
    }
}
