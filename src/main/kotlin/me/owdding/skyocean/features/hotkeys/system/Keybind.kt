package me.owdding.skyocean.features.hotkeys.system

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.ktcodecs.GenerateCodec

@GenerateCodec
data class Keybind(
    val keys: List<InputConstants.Key>,
    val settings: KeybindSettings,
)

