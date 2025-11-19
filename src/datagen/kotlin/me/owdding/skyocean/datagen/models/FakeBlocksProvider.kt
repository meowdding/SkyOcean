package me.owdding.skyocean.datagen.models

import me.owdding.skyocean.datagen.models.factories.*
import me.owdding.skyocean.datagen.providers.SkyOceanModelProvider
import me.owdding.skyocean.events.FakeBlockModelEventRegistrar
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

class FakeBlocksProvider(output: FabricDataOutput, saveBlockStates: Boolean = true, val collector: (FakeBlockModelEventRegistrar) -> Unit) :
    SkyOceanModelProvider(output) {
    val blockStatePathProvider: PackOutput.PathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, BLOCK_STATES_PATH)
    val fakeBlockStateCollector = FakeBlockStateCollector(mutableListOf(), saveBlockStates)
    val context = ModelGenContext(fakeBlockStateCollector, output)

    val factories = listOf(
        SnowLayerFactory,
        GlassPaneFactory,
        WallModelFactory,
        StairModelFactory,
        SlabModelFactory,
        RemapFactory,
        DefaultModelFactory,
    )

    constructor(output: FabricDataOutput) : this(output, collector = { registrar -> RegisterFakeBlocksEvent(registrar).post(SkyBlockAPI.eventBus) })

    override fun run(output: CachedOutput): CompletableFuture<*> = CompletableFuture.allOf(
        super.run(output),
        fakeBlockStateCollector.save(output, blockStatePathProvider),
    )

    override fun generateBlockStateModels(blockModelGenerators: BlockModelGenerators) {
        savedModels.clear()
        factories.forEach { it.generator = blockModelGenerators }

        val fakeBlocks = mutableMapOf<Block, MutableMap<ResourceLocation, Pair<ResourceLocation?, Block>>>()
        fun register(block: Block, texture: Block, definition: ResourceLocation, parent: ResourceLocation?) {
            fakeBlocks.getOrPut(block, ::mutableMapOf)[definition] = (parent to texture)
        }
        collector { block, texture, definition, parent, _ ->
            register(block, texture, definition, parent)
            println("Registering $block")
        }
        fakeBlocks.entries.forEach { (block, entries) ->
            factories.firstOrNull { it.isFor(block) }?.let {
                entries.forEach { model ->
                    val (model, pair) = model
                    val (parent, texture) = pair
                    println("Creating $model")
                    it.create(block, texture, model, parent, blockModelGenerators, context)
                }
            }
        }
    }
}
