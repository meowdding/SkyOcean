package me.owdding.skyocean.datagen.models.factories

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.models.BlockModelFactory
import me.owdding.skyocean.datagen.models.ModelGenContext
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.*

object SnowLayerFactory : BlockModelFactory() {
    override fun isFor(block: Block) = block == Blocks.SNOW

    override fun create(
        block: Block,
        texture: Block,
        fakeBlock: ResourceLocation,
        parent: ResourceLocation?,
        generator: BlockModelGenerators,
        modelGenContext: ModelGenContext,
    ) {
        assert(parent == null) { "Parents aren't supported for snow blocks" }
        val multiVariant = BlockModelGenerators.plainVariant(SkyOcean.id(createCopy(texture, fakeBlock, parent).path))
        modelGenContext.collectState(
            fakeBlock,
            MultiVariantGenerator.dispatch(block).with(
                PropertyDispatch.initial(BlockStateProperties.LAYERS).generate {
                    if (it < 8) {
                        ModelTemplate(
                            Optional.of(ResourceLocation.withDefaultNamespace("block/snow_height${it * 2}")),
                            Optional.empty(),
                            TextureSlot.TEXTURE,
                            TextureSlot.PARTICLE,
                        ).create(
                            getBlockModelLocation(fakeBlock),
                            TextureMapping.defaultTexture(texture),
                        ) { path, model -> modelOutput(path.withSuffix("_height${it * 2}"), model) }
                        BlockModelGenerators.plainVariant(getBlockModelLocation(fakeBlock, "_height${it * 2}"))
                    } else multiVariant
                },
            ),
        )
    }
}
