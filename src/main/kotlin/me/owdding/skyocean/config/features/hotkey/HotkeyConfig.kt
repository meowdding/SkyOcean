package me.owdding.skyocean.config.features.hotkey

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object HotkeyConfig : CategoryKt("hotkeys") {
    override val name = Translated("skyocean.config.hotkeys")

    val enabled by boolean(false) {
        this.translation = "skyocean.config.hotkeys.enabled"
    }

    val sequenceInputDelay by long(250) {
        this.translation = "skyocean.config.hotkeys.sequence_input_delay"
        slider = true
        // 1t <-> 2s
        range = 50L..2000L
    }

    val disabled get() = !enabled

}
