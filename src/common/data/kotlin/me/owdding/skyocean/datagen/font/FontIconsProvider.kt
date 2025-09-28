package me.owdding.skyocean.datagen.font

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.providers.SkyOceanFontProvider
import me.owdding.skyocean.utils.chat.ComponentIcon
import me.owdding.skyocean.utils.chat.ComponentIcons
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput

class FontIconsProvider(output: FabricDataOutput) : SkyOceanFontProvider(output, ComponentIcons.ID) {

    override fun SkyOceanFontProviderHolder.create() {
        ComponentIcon.entries.forEach { it ->
            bitmap(SkyOcean.id("font_icons/${it.image.lowercase()}.png"), 7) { row(it.icon.toString()) }
        }
    }

    override fun getName(): String = "Font Icon"
}
