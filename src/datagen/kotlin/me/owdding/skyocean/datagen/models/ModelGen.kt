package me.owdding.skyocean.datagen.models

import me.owdding.skyocean.datagen.models.factories.DefaultModelFactory
import me.owdding.skyocean.datagen.models.factories.GlassPaneFactory
import me.owdding.skyocean.datagen.models.factories.InfestedStoneFactory
import me.owdding.skyocean.datagen.models.factories.SnowLayerFactory
import me.owdding.skyocean.helpers.fakeblocks.BLOCK_STATES_PATH
import me.owdding.skyocean.helpers.fakeblocks.FakeBlocks
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.data.CachedOutput
import net.minecraft.data.PackOutput
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ReloadableResourceManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ModelGen(output: FabricDataOutput) : FabricModelProvider(output) {
    val blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, BLOCK_STATES_PATH)
    val fakeBlockStateCollector = FakeBlockStateCollector(mutableListOf())
    val context = ModelGenContext(fakeBlockStateCollector, output)

    val factories = listOf(
        SnowLayerFactory,
        GlassPaneFactory,
        InfestedStoneFactory,
        DefaultModelFactory,
    )

    override fun run(output: CachedOutput): CompletableFuture<*> {
        return CompletableFuture.allOf(super.run(output), fakeBlockStateCollector.save(output, blockStatePathProvider))
    }

    override fun generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators) {
        factories.forEach { it.generator = blockStateModelGenerator }
        FakeBlocks.init(ReloadableResourceManager(PackType.CLIENT_RESOURCES), CompletableFuture.delayedExecutor(1, TimeUnit.MILLISECONDS))
        FakeBlocks.fakeBlocks.entries.forEach { (block, entries) ->
            factories.firstOrNull { it.isFor(block) }?.let {
                entries.forEach { model ->
                    println("Creating ${model.first}")
                    it.create(block, model, blockStateModelGenerator, context)
                }
            }
        }
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerators) {

    }
}
