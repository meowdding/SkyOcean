package me.owdding.skyocean.datagen.providers

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.data.DataProvider

interface SkyOceanDataGeneratorEntrypoint : DataGeneratorEntrypoint {

    fun FabricDataGenerator.Pack.register(provider: (FabricDataOutput) -> DataProvider) {
        this.addProvider { provider(it as FabricDataOutput) }
    }

}
