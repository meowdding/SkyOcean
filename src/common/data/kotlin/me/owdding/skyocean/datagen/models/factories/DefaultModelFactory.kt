package me.owdding.skyocean.datagen.models.factories

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.models.BlockModelFactory
import me.owdding.skyocean.datagen.models.ModelGenContext
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block

object DefaultModelFactory : BlockModelFactory() {
    override fun isFor(block: Block) = true

    override fun create(
        block: Block,
        texture: Block,
        fakeBlock: ResourceLocation,
        parent: ResourceLocation?,
        generator: BlockModelGenerators,
        modelGenContext: ModelGenContext,
    ) {
        val multiVariant = BlockModelGenerators.plainVariant(SkyOcean.id(createCopy(texture, fakeBlock, parent).path))
        modelGenContext.collectState(fakeBlock, BlockModelGenerators.createSimpleBlock(block, multiVariant))
    }
}
