package me.owdding.skyocean.features.hotkeys.system

import me.owdding.ktcodecs.GenerateCodec
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import java.util.*

@GenerateCodec
data class HotkeyCategory(
    val identifier: UUID,
    var name: String,
    var username: String = McPlayer.name,
) {
    fun isDefault(): Boolean = this === HotkeyManager.defaultCategory

    fun getHotkeysInCategory(): Collection<Hotkey> = if (isDefault()) {
        HotkeyManager.hotkeys().filter { it.group == null }
    } else {
        HotkeyManager.hotkeys().filter { it.group == identifier }
    }
}
