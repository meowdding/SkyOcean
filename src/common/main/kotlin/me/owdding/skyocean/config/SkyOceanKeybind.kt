package me.owdding.skyocean.config

import me.owdding.lib.utils.MeowddingKeybind
import me.owdding.skyocean.SkyOcean

class SkyOceanKeybind(
    translationKey: String,
    keyCode: Int,
    allowMultipleExecutions: Boolean = false,
    runnable: (() -> Unit)? = null,
) : MeowddingKeybind(SkyOcean.id("main"), translationKey, keyCode, allowMultipleExecutions, runnable)
