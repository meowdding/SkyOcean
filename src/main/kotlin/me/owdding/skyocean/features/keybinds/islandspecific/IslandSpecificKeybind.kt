package me.owdding.skyocean.features.keybinds.islandspecific

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.keysOf
import me.owdding.skyocean.features.keybinds.actions.KeybindAction
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.location.IslandChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient

@GenerateCodec
data class IslandSpecificKeybind(
    val keyCode: Int,
    val islands: List<SkyBlockIsland>,
    val action: KeybindAction,
) {
    val keyInput = keysOf(keyCode)
    val onIsland get() = SkyBlockIsland.inAnyIsland(islands)

    private var isDown: Boolean = false
    private var canBeConsumed = false

    fun tick() {
        val actualState = keyInput.isDown()
        val cachedState = isDown

        if (cachedState == actualState) return
        if (actualState) {
            canBeConsumed = true
        }
        isDown = actualState
    }

    fun consume(): Boolean {
        val state = canBeConsumed
        canBeConsumed = false
        return state
    }

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
            currentKeybinds.addAll(knownKeybinds.filter(IslandSpecificKeybind::onIsland))
        }

        @Subscription(TickEvent::class)
        fun onTick() {
            currentKeybinds.forEach { keybind ->
                keybind.tick()
                if (keybind.consume()) {
                    keybind.action()
                }
            }
        }
    }
}
