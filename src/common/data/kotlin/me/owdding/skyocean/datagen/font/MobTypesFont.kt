package me.owdding.skyocean.datagen.font

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.providers.SkyOceanFontProvider
import me.owdding.skyocean.features.textures.KnownMobIcon
import me.owdding.skyocean.features.textures.MobIcons
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput

class MobTypesFont(output: FabricDataOutput) : SkyOceanFontProvider(output, MobIcons.FONT_ID) {
    override fun SkyOceanFontProviderHolder.create() {
        space {
            add(' ', 4)
            add("\u200C", 0)
        }

        KnownMobIcon.entries.forEach {
            // ${it.name.lowercase()}
            bitmap(SkyOcean.id("font/mob_types/meow.png"), 8) {
                row(it.icon)
            }
        }
    }

    override fun getName() = "Mob Types Font Generator"
}
