package me.owdding.skyocean.utils.nbs

import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent.Companion.argument
import tech.thatgravyboat.skyblockapi.helpers.McClient

@Module
object NBSMusicManager: MeowddingLogger by SkyOcean.featureLogger("NBS Music Manager") {
    private val activePlayers = HashMap<String, NBSPlayer>()

    @Subscription
    context(event: RegisterSkyOceanCommandEvent)
    fun commands() {
        event.registerDev("nbs") {
            thenCallback("start id", StringArgumentType.string()) {
                play("command", argument<String>("id"))
            }
            thenCallback("stop") {
                stop("command")
            }
            thenCallback("resume") {
                resume("command")
            }
            thenCallback("pause") {
                pause("command")
            }
            thenCallback("restart") {
                restart("command")
            }
        }
    }

    fun play(id: String, songResourceLocation: String) {
        stop(id)

        val location = SkyOcean.id("sounds/nbs/$songResourceLocation.nbs")

        try {
            val resourceOptional = McClient.self.resourceManager.getResource(location)
            if (resourceOptional.isEmpty) return

            val bytes = resourceOptional.get().open().use { it.readAllBytes() }
            val song = NBSReader.reader().read(bytes)
            val player = NBSPlayer(song)

            activePlayers[id] = player
            player.start()
        } catch (e: Exception) {
            error("Failed to play $songResourceLocation with id: $id", e)
        }
    }

    fun stop(id: String) {
        activePlayers.remove(id)?.stop()
    }

    fun stopAll() {
        activePlayers.keys.forEach { stop(it) }
    }

    fun restart(id: String) = activePlayers[id]?.restart()

    fun pause(id: String) = activePlayers[id]?.pause()

    fun resume(id: String) = activePlayers[id]?.resume()


    fun isMusicActive(id: String): Boolean = activePlayers.containsKey(id)
}
