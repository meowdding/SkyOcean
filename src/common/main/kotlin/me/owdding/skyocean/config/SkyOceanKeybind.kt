package me.owdding.skyocean.config

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.lib.utils.MeowddingKeybind
import me.owdding.skyocean.SkyOcean

class SkyOceanKeybind(
    translationKey: String,
    keyCode: Int = InputConstants.UNKNOWN.value,
    allowMultipleExecutions: Boolean = false,
    runnable: (() -> Unit)? = null,
) : MeowddingKeybind(SkyOcean.id("main"), "skyocean.keybind.$translationKey", keyCode, allowMultipleExecutions, runnable)
