package me.owdding.skyocean.datagen.dispatcher

import me.owdding.skyocean.SkyOcean
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

data class SkyOceanDataGenerator(val parent: FabricDataGenerator) {
    fun createPack(): FabricDataGenerator.Pack = parent.createPack()

    fun createResourcePack(name: String): FabricDataGenerator.Pack {
        SkyOceanDatagenDispatcher.createResourcePack(name)
        return parent.createBuiltinResourcePack(SkyOcean.id(name))
    }

}
