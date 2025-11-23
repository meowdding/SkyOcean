package me.owdding.skyocean.datagen.models

import com.google.common.collect.Maps
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.renderer.block.model.BlockModelDefinition
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput.PathProvider
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

typealias BlockStateEntry = Pair<ResourceLocation, BlockModelDefinitionGenerator>

data class FakeBlockStateCollector(val list: MutableList<BlockStateEntry>, val saveBlockStates: Boolean) {
    fun save(cachedOutput: CachedOutput, pathProvider: PathProvider): CompletableFuture<*> {
        if (!saveBlockStates) return CompletableFuture.completedFuture(null)
        val map = Maps.transformValues(this.list.toMap(), BlockModelDefinitionGenerator::create)
        return DataProvider.saveAll(cachedOutput, BlockModelDefinition.CODEC, { pathProvider.json(it) }, map)
    }
}

data class ModelGenContext(private val fakeBlockStateCollector: FakeBlockStateCollector, val output: FabricDataOutput) {
    fun collectState(location: ResourceLocation, generator: BlockModelDefinitionGenerator) {
        fakeBlockStateCollector.list.add(location to generator)
    }
}
