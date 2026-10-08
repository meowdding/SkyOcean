package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.fishing.FishingConfig
import net.minecraft.client.renderer.block.FluidModel
import net.minecraft.client.renderer.chunk.ChunkSectionLayer
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object LavaReplacement {
    private var opaqueWaterModel: FluidModel? = null

    private fun isActive(): Boolean = SkyBlockIsland.CRIMSON_ISLE.inIsland() && FishingConfig.lavaReplacement

    @JvmStatic
    fun onModelsBaked(models: Map<Fluid, FluidModel>) {
        opaqueWaterModel = models[Fluids.WATER]?.let {
            FluidModel(ChunkSectionLayer.SOLID, it.stillMaterial, it.flowingMaterial, it.overlayMaterial, it.tintSource)
        }
    }

    @JvmStatic
    fun getReplacementModel(state: FluidState, original: FluidModel): FluidModel {
        if (!isActive()) return original
        return when (state.type) {
            Fluids.LAVA, Fluids.FLOWING_LAVA -> opaqueWaterModel ?: original
            else -> original
        }
    }
}
