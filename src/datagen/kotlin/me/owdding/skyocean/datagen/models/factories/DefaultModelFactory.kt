package me.owdding.skyocean.datagen.models.factories

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.models.BlockModelFactory
import me.owdding.skyocean.datagen.models.ModelGenContext
import me.owdding.skyocean.helpers.fakeblocks.FakeBlockEntry
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.world.level.block.Block

object DefaultModelFactory : BlockModelFactory() {
    override fun isFor(block: Block) = true

    override fun create(block: Block, fakeBlock: FakeBlockEntry, generator: BlockModelGenerators, modelGenContext: ModelGenContext) {
        val multiVariant = BlockModelGenerators.plainVariant(SkyOcean.id(createCopy(block, fakeBlock).path))
        modelGenContext.collectState(fakeBlock.first, BlockModelGenerators.createSimpleBlock(block, multiVariant))
    }
}
