package me.owdding.skyocean.datagen.dispatcher

import me.owdding.skyocean.SkyOcean
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.SharedConstants
import net.minecraft.data.DataGenerator

data class SkyOceanDataGenerator(val parent: FabricDataGenerator) : DataGenerator(parent.rootOutputFolder, SharedConstants.getCurrentVersion(), true) {
    fun createPack(): FabricDataGenerator.Pack = parent.createPack()

    fun createResourcePack(name: String): FabricDataGenerator.Pack {
        SkyOceanDatagenDispatcher.createResourcePack(name)
        return parent.createBuiltinResourcePack(SkyOcean.id(name))
    }

}
