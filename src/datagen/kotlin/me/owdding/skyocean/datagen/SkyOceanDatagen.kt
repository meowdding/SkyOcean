package me.owdding.skyocean.datagen

import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGenerator
import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGeneratorEntrypoint
import me.owdding.skyocean.datagen.font.FontIconsProvider
import me.owdding.skyocean.datagen.font.MobTypesFontProvider
import me.owdding.skyocean.features.textures.KnownMobIcon
import me.owdding.skyocean.features.textures.MobIcons

object SkyOceanDatagen : SkyOceanDataGeneratorEntrypoint() {
    override val name: String = "included"

    override fun run(output: SkyOceanDataGenerator) {
        val createPack = output.createPack()
        createPack.register { MobTypesFontProvider(it, KnownMobIcon::name, MobIcons.MOB_ICONS) }
        createPack.register { MobTypesFontProvider(it, KnownMobIcon::short, MobIcons.MOB_ICONS_SHORT) }
        createPack.register { FontIconsProvider(it) }
        createPack.addProvider(::EntityTagProvider)
    }
}
