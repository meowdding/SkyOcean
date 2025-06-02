package me.owdding.skyocean.datagen

import me.owdding.skyocean.datagen.models.ModelGen
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput

object SkyOceanDatagen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val createPack = fabricDataGenerator.createPack()
        createPack.addProvider { ModelGen(it as FabricDataOutput) }
    }
}
