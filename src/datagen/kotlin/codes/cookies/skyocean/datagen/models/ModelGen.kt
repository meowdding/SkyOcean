package codes.cookies.skyocean.datagen.models

import codes.cookies.skyocean.SkyOcean
import codes.cookies.skyocean.datagen.models.factories.DefaultModelFactory
import codes.cookies.skyocean.datagen.models.factories.GlassPaneFactory
import codes.cookies.skyocean.datagen.models.factories.SnowLayerFactory
import codes.cookies.skyocean.features.textures.GemstoneBlocks
import codes.cookies.skyocean.helpers.fakeblocks.BLOCK_STATES_PATH
import codes.cookies.skyocean.helpers.fakeblocks.FakeBlocks
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_270
import net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_90
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.blockstates.MultiPartGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.*
import net.minecraft.data.CachedOutput
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ReloadableResourceManager
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ModelGen(output: FabricDataOutput) : FabricModelProvider(output) {
    val blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, BLOCK_STATES_PATH)
    val fakeBlockStateCollector = FakeBlockStateCollector(mutableListOf())
    val context = ModelGenContext(fakeBlockStateCollector, output)

    val factories = listOf(
        SnowLayerFactory,
        GlassPaneFactory,
        DefaultModelFactory,
    )

    override fun run(output: CachedOutput): CompletableFuture<*> {
        return CompletableFuture.allOf(super.run(output), fakeBlockStateCollector.save(output, blockStatePathProvider))
    }

    override fun generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators) {
        FakeBlocks.init(ReloadableResourceManager(PackType.CLIENT_RESOURCES), CompletableFuture.delayedExecutor(1, TimeUnit.MILLISECONDS))
        FakeBlocks.fakeBlocks.entries.forEach { (block, entries) ->
            factories.firstOrNull { it.isFor(block) }?.let {
                entries.forEach { model ->
                    it.create(block, model, blockStateModelGenerator, context)
                }
            }
        }
    }

    private fun createGemstones(generator: BlockModelGenerators) {
        GemstoneBlocks.map.entries.chunked(2) {
            val (originalBlock, newBlock) = it.first()
            val (originalPane, newPane) = it.last()
            createGlassAndGlassPane(originalBlock, originalPane, newBlock, newPane, generator)
        }
    }

    fun createGlassAndGlassPane(original: Block, originalPane: Block, new: Block, newPane: Block, generator: BlockModelGenerators) {
        createCopy(original, new, generator)
        createGlassPane(original, originalPane, newPane, generator)
    }

    private fun createCarpetCopy(wool: Block, destination: Block, generator: BlockModelGenerators) {
        val multiVariant = BlockModelGenerators.plainVariant(TexturedModel.CARPET.get(wool).create(destination, generator.modelOutput))
        generator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(destination, multiVariant))
    }

    private fun createCopy(origin: Block, destination: Block, generator: BlockModelGenerators) {
        val textureMapping = TextureMapping.cube(origin)
        val multiVariant = BlockModelGenerators.plainVariant(
            SkyOcean.id(
                ModelTemplates.CUBE_ALL.create(
                    destination,
                    textureMapping,
                    generator.modelOutput,
                ).path,
            ),
        )
        generator.blockStateOutput.accept(
            BlockModelGenerators.createSimpleBlock(destination, multiVariant),
        )
    }

    private fun createGlassPane(reference: Block, originalPane: Block, pane: Block, generator: BlockModelGenerators) {
        val textureMapping = TextureMapping.pane(reference, originalPane)
        val post = BlockModelGenerators.plainVariant(ModelTemplates.STAINED_GLASS_PANE_POST.create(pane, textureMapping, generator.modelOutput))
        val side = BlockModelGenerators.plainVariant(ModelTemplates.STAINED_GLASS_PANE_SIDE.create(pane, textureMapping, generator.modelOutput))
        val sideAlt = BlockModelGenerators.plainVariant(ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(pane, textureMapping, generator.modelOutput))
        val noSide = BlockModelGenerators.plainVariant(ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(pane, textureMapping, generator.modelOutput))
        val noSideAlt = BlockModelGenerators.plainVariant(ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(pane, textureMapping, generator.modelOutput))
    MultiPartGenerator.multiPart(pane).with(post)
        .with(BlockModelGenerators.condition().term(BlockStateProperties.NORTH, true), side)
        .with(BlockModelGenerators.condition().term(BlockStateProperties.EAST, true), side.with(Y_ROT_90))
        .with(BlockModelGenerators.condition().term(BlockStateProperties.SOUTH, true), sideAlt)
        .with(BlockModelGenerators.condition().term(BlockStateProperties.WEST, true), sideAlt.with(Y_ROT_90))
        .with(BlockModelGenerators.condition().term(BlockStateProperties.NORTH, false), noSide)
        .with(BlockModelGenerators.condition().term(BlockStateProperties.EAST, false), noSideAlt)
        .with(BlockModelGenerators.condition().term(BlockStateProperties.SOUTH, false), noSideAlt.with(Y_ROT_90))
        .with(BlockModelGenerators.condition().term(BlockStateProperties.WEST, false), noSide.with(Y_ROT_270))
        .let { generator.blockStateOutput.accept(it) }
    }

    private fun createSnowBlocks(fullBlock: Block, layerBlock: Block, generator: BlockModelGenerators) {
        val textureMapping = TextureMapping.cube(Blocks.SNOW)
        val multiVariant = BlockModelGenerators.plainVariant(
            SkyOcean.id(
                ModelTemplates.CUBE_ALL.create(
                    fullBlock,
                    textureMapping,
                    generator.modelOutput,
                ).path,
            ),
        )
        generator.blockStateOutput.accept(
            MultiVariantGenerator.dispatch(layerBlock).with(
                PropertyDispatch.initial(BlockStateProperties.LAYERS).generate {
                    if (it < 8) {
                        ModelTemplate(
                            Optional.of(ResourceLocation.withDefaultNamespace("block/snow_height${it * 2}")),
                            Optional.empty(),
                            TextureSlot.TEXTURE,
                            TextureSlot.PARTICLE,
                        ).create(
                            layerBlock,
                            TextureMapping.defaultTexture(Blocks.SNOW),
                        ) { path, model -> generator.modelOutput.accept(path.withSuffix("_height${it * 2}"), model) }
                        BlockModelGenerators.plainVariant(
                            ModelLocationUtils.getModelLocation(
                                layerBlock,
                                "_height${it * 2}",
                            ),
                        )
                    } else multiVariant
                },
            ),
        )
        generator.blockStateOutput.accept(
            BlockModelGenerators.createSimpleBlock(fullBlock, multiVariant),
        )
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerators) {

    }
}
