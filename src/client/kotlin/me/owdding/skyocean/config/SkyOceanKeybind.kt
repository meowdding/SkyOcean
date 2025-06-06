package me.owdding.skyocean.config

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping

data class SkyOceanKeybind(private val translationKey: String, private val keyCode: Int) {
    val key: KeyMapping = KeyBindingHelper.registerKeyBinding(KeyMapping(translationKey, keyCode, "skyocean"))

    fun matches(keyCode: Int, scancode: Int) = key.matches(keyCode, scancode)

}
