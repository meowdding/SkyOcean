package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.fishing.FishingConfig
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.WaterFluid
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

//? if >= 26.1 {
import net.minecraft.client.renderer.block.FluidModel
import net.minecraft.client.renderer.block.FluidStateModelSet
//? } else {
/*import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap
import net.minecraft.client.renderer.RenderType
*///? }

abstract class OpaqueWaterFluid : WaterFluid() {
    object Flowing : OpaqueWaterFluid() {
        override fun createFluidStateDefinition(builder: StateDefinition.Builder<Fluid, FluidState>) {
            super.createFluidStateDefinition(builder)
            builder.add(LEVEL)
        }

        override fun getAmount(fluidState: FluidState): Int = fluidState.getValue(LEVEL)
        override fun isSource(fluidState: FluidState): Boolean = false
    }

    object Source : OpaqueWaterFluid() {
        override fun getAmount(fluidState: FluidState): Int = 8
        override fun isSource(fluidState: FluidState): Boolean = true
    }
}


@Module
object LavaReplacement {
    init {
        // Force initialize vanilla fluid registry to avoid load order race conditions
        Class.forName("net.minecraft.world.level.material.Fluids")
    }

    @JvmField
    val OPAQUE_WATER: Fluid = Registry.register(
        BuiltInRegistries.FLUID,
        SkyOcean.id("opaque_water"),
        OpaqueWaterFluid.Source
    )

    @JvmField
    val OPAQUE_FLOWING_WATER: Fluid = Registry.register(
        BuiltInRegistries.FLUID,
        SkyOcean.id("opaque_flowing_water"),
        OpaqueWaterFluid.Flowing
    )

    //? if >= 26.1 {
    @JvmField
    val OPAQUE_WATER_MODEL: FluidModel.Unbaked = FluidModel.Unbaked(
        FluidStateModelSet.WATER_MODEL.stillMaterial(),
        FluidStateModelSet.WATER_MODEL.flowingMaterial(),
        FluidStateModelSet.WATER_MODEL.overlayMaterial(),
        FluidStateModelSet.WATER_MODEL.tintSource()
    )
    //? } else {
    /*init {
        FluidRenderHandlerRegistry.INSTANCE.register(
            OPAQUE_WATER,
            OPAQUE_FLOWING_WATER,
            SimpleFluidRenderHandler(
                ResourceLocation.withDefaultNamespace("block/water_still"),
                ResourceLocation.withDefaultNamespace("block/water_flow"),
                ResourceLocation.withDefaultNamespace("block/water_overlay")
            )
        )
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.solid(), OPAQUE_WATER, OPAQUE_FLOWING_WATER)
    }
    *///? }

    fun isActive(): Boolean = SkyBlockIsland.CRIMSON_ISLE.inIsland() && FishingConfig.lavaReplacement
}
