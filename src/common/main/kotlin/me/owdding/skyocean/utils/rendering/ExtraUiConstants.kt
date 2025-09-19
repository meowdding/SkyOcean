package me.owdding.skyocean.utils.rendering

import earth.terrarium.olympus.client.ui.UIConstants
import net.minecraft.client.gui.components.WidgetSprites

object ExtraUiConstants {
    val alwaysDisabledDarkButton = WidgetSprites(
        UIConstants.DARK_BUTTON.disabled(),
        UIConstants.DARK_BUTTON.disabledFocused(),
    )
}
