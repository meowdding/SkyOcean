package me.owdding.skyocean.features.keybinds.islandspecific

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.keysOf
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.location.IslandChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient

@GenerateCodec
data class IslandSpecificKeybind(
    val keyCode: Int,
    val islands: List<SkyBlockIsland>,
    val command: String,
) {
    val keyInput = keysOf(keyCode)
    val onIsland get() = SkyBlockIsland.inAnyIsland(islands)


    @Module
    companion object {
        private val knownKeybinds = mutableListOf<IslandSpecificKeybind>()
        private val currentKeybinds = mutableListOf<IslandSpecificKeybind>()

        fun register(keybind: IslandSpecificKeybind) {
            knownKeybinds.add(keybind)
            if (keybind.onIsland) {
                currentKeybinds.add(keybind)
            }
        }

        @Subscription(IslandChangeEvent::class)
        fun onIsland() {
            currentKeybinds.clear()
            knownKeybinds.forEach { keybind ->
                if (keybind.onIsland) {
                    currentKeybinds.add(keybind)
                }
            }
        }

        @Subscription(TickEvent::class)
        fun onTick() {
            currentKeybinds.forEach { keybind ->
                // No consume im killing myself
                if (keybind.keyInput.isDown()) {
                    McClient.sendClientCommand(keybind.command)
                }
            }
        }
    }
}
