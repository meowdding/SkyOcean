package me.owdding.skyocean.datagen

import me.owdding.skyocean.datagen.font.MobTypesFontProvider
import me.owdding.skyocean.datagen.models.FakeBlocksProvider
import me.owdding.skyocean.datagen.providers.SkyOceanDataGeneratorEntrypoint
import me.owdding.skyocean.features.textures.KnownMobIcon
import me.owdding.skyocean.features.textures.MobIcons
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object SkyOceanDatagen : SkyOceanDataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val createPack = fabricDataGenerator.createPack()
        createPack.register(::FakeBlocksProvider)
        createPack.register { MobTypesFontProvider(it, KnownMobIcon::name, MobIcons.MOB_ICONS) }
        createPack.register { MobTypesFontProvider(it, KnownMobIcon::short, MobIcons.MOB_ICONS_SHORT) }
    }
}
