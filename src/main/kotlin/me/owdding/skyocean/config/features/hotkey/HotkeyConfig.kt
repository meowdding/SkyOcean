package me.owdding.skyocean.config.features.hotkey

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen

object HotkeyConfig : CategoryKt("hotkeys") {
    override val name = Translated("skyocean.config.hotkeys")

    val enabled by boolean(true) {
        this.translation = "skyocean.config.hotkeys.enabled"
    }

    val sequenceInputDelay by long(250) {
        this.translation = "skyocean.config.hotkeys.sequence_input_delay"
        slider = true
        // 1t <-> 2s
        range = 50L..2000L
    }

    init {
        button {
            title = "skyocean.config.hotkeys.edit"
            text = "Open"
            description = "skyocean.config.hotkeys.edit.desc"
            onClick {
                McClient.setScreen(McScreen.self?.let { me.owdding.skyocean.features.hotkeys.ConditionalHotkeyScreen })
            }
        }
    }

    val disabled get() = !enabled

}
