package me.owdding.skyocean.features.hotkeys.system

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.OptionalBoolean
import me.owdding.skyocean.features.hotkeys.actions.HotkeyAction
import me.owdding.skyocean.features.hotkeys.conditions.HotkeyCondition
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.components.CatppuccinColors
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.UUID

/**
 * Terminology
 *
 * Hotkey    Any one action that can be bound to keys
 * Keybind   The key combination that invokes a hotkey
 *
 */
@GenerateCodec
data class Hotkey(
    var keybind: Keybind,
    var action: HotkeyAction,
    var condition: HotkeyCondition,
    var name: String,
    @OptionalBoolean(true) var enabled: Boolean = true,
    val group: UUID?,
    @FieldName("created_at") val timeCreated: Long = System.currentTimeMillis(),
) {
    fun isActive() = enabled && condition.test()

    fun invoke() {
        action()
    }

    fun formatKeys(override: Boolean? = null) = formatKeys(keybind.keys, override ?: keybind.settings.orderSensitive)
    companion object {
        fun formatKeys(keys: List<InputConstants.Key>, orderSensitive: Boolean) = Text.join(
            keys.map {
                Text.of {
                    append(it.displayName)
                    this.color = CatppuccinColors.Mocha.teal
                }
            },
            separator = if (orderSensitive) !" » " else !" + ",
        ) {
            color = CatppuccinColors.Mocha.text
        }
    }
}
