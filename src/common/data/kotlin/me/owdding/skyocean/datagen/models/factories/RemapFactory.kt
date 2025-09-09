package me.owdding.skyocean.datagen.models.factories

import me.owdding.skyocean.datagen.models.BlockModelFactory
import me.owdding.skyocean.datagen.models.ModelGenContext
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

object RemapFactory : BlockModelFactory() {
    private val blocks = mapOf(
        Blocks.INFESTED_CHISELED_STONE_BRICKS to Blocks.CHISELED_STONE_BRICKS,
        Blocks.INFESTED_COBBLESTONE to Blocks.COBBLESTONE,
        Blocks.INFESTED_CRACKED_STONE_BRICKS to Blocks.CRACKED_STONE_BRICKS,
        Blocks.INFESTED_DEEPSLATE to Blocks.DEEPSLATE,
        Blocks.INFESTED_MOSSY_STONE_BRICKS to Blocks.MOSSY_STONE_BRICKS,
        Blocks.INFESTED_STONE to Blocks.STONE,
        Blocks.INFESTED_STONE_BRICKS to Blocks.STONE_BRICKS,
    )

    override fun isFor(block: Block) = blocks.contains(block)

    override fun create(
        block: Block,
        texture: Block,
        fakeBlock: ResourceLocation,
        parent: ResourceLocation?,
        generator: BlockModelGenerators,
        modelGenContext: ModelGenContext,
    ) {
        val actualBlock = blocks.getOrElse(block) {
            error("Unhandled block $block")
        }

        DefaultModelFactory.create(actualBlock, texture.takeUnless { it == block } ?: actualBlock, fakeBlock, parent, generator, modelGenContext)
    }
}
