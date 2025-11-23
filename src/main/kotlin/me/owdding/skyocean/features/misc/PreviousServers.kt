package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Module
object PreviousServers {

    private val lastServers: MutableList<Server> = mutableListOf()

    @Subscription
    fun onServerChange(event: ServerChangeEvent) {
        if (!MiscConfig.previousServer) return

        // Update time of the last server because we still want to alert about being in it,
        // even when one joined it ages ago.
        lastServers.maxByOrNull { it.lastTimeInServer }?.let { it.lastTimeInServer = currentInstant().minus(1.seconds) }

        lastServers.removeIf { it.lastTimeInServer.since() > MiscConfig.previousServerTime.seconds }

        lastServers.find { it.name == event.name }?.let {
            Text.of {
                append("You've already been on this server ")
                append(it.lastTimeInServer.since().toReadableTime())
                append(" ago")
            }.sendWithPrefix()
            it.lastTimeInServer = currentInstant()
        } ?: run {
            lastServers.add(Server(event.name, currentInstant()))
        }
    }

    private data class Server(
        val name: String,
        var lastTimeInServer: Instant,
    )

}
