package me.owdding.skyocean.utils.nbs

import me.owdding.skyocean.SkyOcean
import me.owdding.lib.utils.MeowddingLogger
import tech.thatgravyboat.skyblockapi.helpers.McClient

object NBSMusicManager: MeowddingLogger by SkyOcean.featureLogger("NBS Music Manager") {
    private val activePlayers = HashMap<String, NBSPlayer>()

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
