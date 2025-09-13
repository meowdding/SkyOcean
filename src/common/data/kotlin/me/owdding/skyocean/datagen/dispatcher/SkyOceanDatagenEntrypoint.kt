package me.owdding.skyocean.datagen.dispatcher

import me.owdding.skyocean.datagen.SkyOceanDatagen
import me.owdding.skyocean.datagen.resourcepacks.SkyOceanDarkModeMist
import me.owdding.skyocean.datagen.resourcepacks.SkyOceanDeepHollows
import me.owdding.skyocean.datagen.resourcepacks.SkyOceanOvergrownTunnels
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object SkyOceanDatagenEntrypoint : DataGeneratorEntrypoint {
    val children = mutableListOf(
        SkyOceanDatagen,
        SkyOceanDeepHollows,
        SkyOceanOvergrownTunnels,
        SkyOceanDarkModeMist,
    )

    override fun onInitializeDataGenerator(output: FabricDataGenerator) {
        SkyOceanDatagenDispatcher.register()
        children.forEach { it.onInitializeDataGenerator(output) }
    }
}
