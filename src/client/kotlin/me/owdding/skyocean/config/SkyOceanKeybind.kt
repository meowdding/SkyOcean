package me.owdding.skyocean.config

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyPressedEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent

data class SkyOceanKeybind(private val translationKey: String, private val keyCode: Int) {
    val key: KeyMapping = KeyBindingHelper.registerKeyBinding(KeyMapping(translationKey, keyCode, "skyocean"))

    val isDown get() = key.isDown

    fun matches(keyCode: Int, scancode: Int) = key.matches(keyCode, scancode)
    fun matches(event: ScreenKeyReleasedEvent) = matches(event.key, event.scanCode)
    fun matches(event: ScreenKeyPressedEvent) = matches(event.key, event.scanCode)
}
