package me.owdding.skyocean.datagen.models

import me.owdding.skyocean.datagen.models.factories.DefaultModelFactory
import me.owdding.skyocean.datagen.models.factories.GlassPaneFactory
import me.owdding.skyocean.datagen.models.factories.InfestedStoneFactory
import me.owdding.skyocean.datagen.models.factories.SnowLayerFactory
import me.owdding.skyocean.datagen.providers.SkyOceanModelProvider
import me.owdding.skyocean.events.RegisterFakeBlocksEvent
import me.owdding.skyocean.helpers.BLOCK_STATES_PATH
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.data.CachedOutput
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import java.util.concurrent.CompletableFuture

class FakeBlocksProvider(output: FabricDataOutput) : SkyOceanModelProvider(output) {
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

    override fun generateBlockStateModels(blockModelGenerators: BlockModelGenerators) {
        factories.forEach { it.generator = blockModelGenerators }

        val fakeBlocks = mutableMapOf<Block, MutableList<ResourceLocation>>()
        fun register(block: Block, definition: ResourceLocation) {
            fakeBlocks.getOrPut(block, ::mutableListOf).add(definition)
        }
        RegisterFakeBlocksEvent { block, definition, predicate ->
            register(block, definition)
            println("Registering $block")
        }.post(SkyBlockAPI.eventBus)
        println("meow")
        fakeBlocks.entries.forEach { (block, entries) ->
            factories.firstOrNull { it.isFor(block) }?.let {
                entries.forEach { model ->
                    println("Creating $model")
                    it.create(block, model, blockModelGenerators, context)
                }
            }
        }
    }
}
