package codes.cookies.skyocean.datagen.models

import codes.cookies.skyocean.SkyOcean
import codes.cookies.skyocean.features.textures.GlaciteBlocks
import codes.cookies.skyocean.features.textures.MistBlocks
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
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.*

class ModelGen(output: FabricDataOutput) : FabricModelProvider(output) {
    override fun generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators) {
        createCopy(Blocks.PACKED_ICE, GlaciteBlocks.GLACITE, blockStateModelGenerator)
        createCopy(Blocks.STONE, GlaciteBlocks.GLACITE_HARD_STONE, blockStateModelGenerator)
        createCopy(Blocks.LIGHT_GRAY_WOOL, GlaciteBlocks.GLACITE_HARD_STONE_WOOL, blockStateModelGenerator)
        createSnowBlocks(GlaciteBlocks.GLACITE_SNOW_BLOCK, GlaciteBlocks.GLACITE_SNOW, blockStateModelGenerator)

        createCopy(Blocks.ICE, MistBlocks.MIST_ICE, blockStateModelGenerator)
        createCopy(Blocks.CLAY, MistBlocks.MIST_CLAY, blockStateModelGenerator)
        createCopy(Blocks.WHITE_STAINED_GLASS, MistBlocks.MIST_MAIN_GLASS, blockStateModelGenerator)
        createSnowBlocks(MistBlocks.MIST_SNOW_BLOCK, MistBlocks.MIST_SNOW, blockStateModelGenerator)
        createGlassPane(Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, MistBlocks.MIST_GLASS_PANE, blockStateModelGenerator)
        createCarpetCopy(Blocks.WHITE_WOOL, MistBlocks.MIST_CARPET, blockStateModelGenerator)
        createCopy(Blocks.LIGHT_BLUE_STAINED_GLASS, MistBlocks.MIST_GLASS, blockStateModelGenerator)
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
