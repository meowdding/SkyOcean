package codes.cookies.skyocean.datagen.models.factories

import codes.cookies.skyocean.SkyOcean
import codes.cookies.skyocean.datagen.models.BlockModelFactory
import codes.cookies.skyocean.datagen.models.ModelGenContext
import codes.cookies.skyocean.helpers.fakeblocks.FakeBlockEntry
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.world.level.block.Block

object DefaultModelFactory : BlockModelFactory {
    override fun isFor(block: Block) = true

    override fun create(block: Block, fakeBlock: FakeBlockEntry, generator: BlockModelGenerators, modelGenContext: ModelGenContext) {
        val textureMapping = TextureMapping.cube(block)
        val multiVariant = BlockModelGenerators.plainVariant(
            SkyOcean.id(
                ModelTemplates.CUBE_ALL.create(
                    fakeBlock.first.withPrefix("block/"),
                    textureMapping,
                    generator.modelOutput,
                ).path,
            ),
        )

        modelGenContext.collectState(fakeBlock.first, BlockModelGenerators.createSimpleBlock(block, multiVariant),)
    }
}
