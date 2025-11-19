package me.owdding.skyocean.datagen.dispatcher

import me.owdding.lib.utils.MeowddingLogger
import me.owdding.skyocean.SkyOcean
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.data.DataProvider

abstract class SkyOceanDataGeneratorEntrypoint(val target: DatagenTarget) : DataGeneratorEntrypoint, MeowddingLogger by SkyOcean.featureLogger("datagen") {
    abstract val name: String

    constructor() : this(DatagenTarget.INCLUDED)

    override fun onInitializeDataGenerator(output: FabricDataGenerator) {
        if (target != SkyOceanDatagenDispatcher.target) {
            info("Skipping $name as target $target doesn't match active target ${SkyOceanDatagenDispatcher.target}")
            return
        }
        run(SkyOceanDataGenerator(output))
    }

    abstract fun run(output: SkyOceanDataGenerator)

    fun FabricDataGenerator.Pack.register(provider: (FabricDataOutput) -> DataProvider) {
        this.addProvider { provider(it as FabricDataOutput) }
    }
}
