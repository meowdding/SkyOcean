package me.owdding.skyocean.datagen.font

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.providers.SkyOceanFontProvider
import me.owdding.skyocean.features.hotkeys.ConditionalHotkeyScreen
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput

class HotkeyFontProvider(output: FabricPackOutput) : SkyOceanFontProvider(output, ConditionalHotkeyScreen.FONT) {
    override fun SkyOceanFontProviderHolder.create() {
        bitmap(SkyOcean.id("font/chevron_up.png"), 8) {
            row("^")
        }
        bitmap(SkyOcean.id("font/chevron_down.png"), 8) {
            row("v")
        }
    }

    override fun getName(): String = "chevron provider"
}
