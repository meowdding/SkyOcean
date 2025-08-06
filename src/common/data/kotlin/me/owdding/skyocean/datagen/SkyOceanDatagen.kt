package me.owdding.skyocean.datagen

import me.owdding.skyocean.datagen.font.MobTypesFont
import me.owdding.skyocean.datagen.models.ModelGen
import me.owdding.skyocean.features.textures.KnownMobIcon
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput

object SkyOceanDatagen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val createPack = fabricDataGenerator.createPack()
        createPack.addProvider { ModelGen(it as FabricDataOutput) }
        createPack.addProvider { MobTypesFont(it as FabricDataOutput, KnownMobIcon::name, "mob_icons") }
        createPack.addProvider { MobTypesFont(it as FabricDataOutput, { it.short.uppercase() }, "mob_icons/short") }
    }
}
