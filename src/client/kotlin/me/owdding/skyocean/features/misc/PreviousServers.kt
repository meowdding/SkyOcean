package me.owdding.skyocean.features.misc

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration.Companion.seconds

@Module
object PreviousServers {

    private val lastServers: MutableList<Server> = mutableListOf()

    @Subscription
    fun onServerChange(event: ServerChangeEvent) {
        if (!MiscConfig.previousServer) return

        // Update time of the last server because we still want to alert about being in it,
        // even when one joined it ages ago.
        lastServers.maxByOrNull { it.lastTimeInServer }?.let { it.lastTimeInServer = Clock.System.now().minus(1.seconds) }

        lastServers.removeIf { it.lastTimeInServer.since() > MiscConfig.previousServerTime.seconds }

        lastServers.find { it.name == event.name }?.let {
            Text.of {
                append("You've already been on this server ")
                append(it.lastTimeInServer.since().toReadableTime())
                append(" ago")
            }.sendWithPrefix()
            it.lastTimeInServer = Clock.System.now()
        } ?: run {
            lastServers.add(Server(event.name, Clock.System.now()))
        }
    }

    private data class Server(
        val name: String,
        var lastTimeInServer: Instant,
    )

}
