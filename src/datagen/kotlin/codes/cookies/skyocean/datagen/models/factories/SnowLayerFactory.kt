package codes.cookies.skyocean.datagen.models.factories

import codes.cookies.skyocean.SkyOcean
import codes.cookies.skyocean.datagen.models.BlockModelFactory
import codes.cookies.skyocean.datagen.models.ModelGenContext
import codes.cookies.skyocean.helpers.fakeblocks.FakeBlockEntry
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.*

object SnowLayerFactory : BlockModelFactory {
    override fun isFor(block: Block) = block == Blocks.SNOW

    override fun create(block: Block, fakeBlock: FakeBlockEntry, generator: BlockModelGenerators, modelGenContext: ModelGenContext) {
        val textureMapping = TextureMapping.cube(Blocks.SNOW)
        val multiVariant = BlockModelGenerators.plainVariant(
            SkyOcean.id(
                ModelTemplates.CUBE_ALL.create(
                    getBlockModelLocation(fakeBlock.first),
                    textureMapping,
                    generator.modelOutput,
                ).path,
            ),
        )
        modelGenContext.collectState(
            fakeBlock.first,
            MultiVariantGenerator.dispatch(block).with(
                PropertyDispatch.initial(BlockStateProperties.LAYERS).generate {
                    if (it < 8) {
                        ModelTemplate(
                            Optional.of(ResourceLocation.withDefaultNamespace("block/snow_height${it * 2}")),
                            Optional.empty(),
                            TextureSlot.TEXTURE,
                            TextureSlot.PARTICLE,
                        ).create(
                            getBlockModelLocation(fakeBlock.first),
                            TextureMapping.defaultTexture(Blocks.SNOW),
                        ) { path, model -> generator.modelOutput.accept(path.withSuffix("_height${it * 2}"), model) }
                        BlockModelGenerators.plainVariant(getBlockModelLocation(fakeBlock.first, "_height${it * 2}"))
                    } else multiVariant
                },
            ),
        )
        //modelGenContext.collectState(fakeBlock.first, BlockModelGenerators.createSimpleBlock(Blocks.SNOW_BLOCK, multiVariant))
    }
}
