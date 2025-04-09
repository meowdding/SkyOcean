package codes.cookies.skyocean.datagen.models.factories

import codes.cookies.skyocean.datagen.models.BlockModelFactory
import codes.cookies.skyocean.datagen.models.ModelGenContext
import codes.cookies.skyocean.helpers.fakeblocks.FakeBlockEntry
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

object InfestedStoneFactory : BlockModelFactory {
    private val blocks = listOf(
        Blocks.INFESTED_CHISELED_STONE_BRICKS,
        Blocks.INFESTED_COBBLESTONE,
        Blocks.INFESTED_CRACKED_STONE_BRICKS,
        Blocks.INFESTED_DEEPSLATE,
        Blocks.INFESTED_MOSSY_STONE_BRICKS,
        Blocks.INFESTED_STONE,
        Blocks.INFESTED_STONE_BRICKS,
    )

    override fun isFor(block: Block) = blocks.contains(block)

    override fun create(block: Block, fakeBlock: FakeBlockEntry, generator: BlockModelGenerators, modelGenContext: ModelGenContext) {
        val actualBlock = when (block) {
            Blocks.INFESTED_CHISELED_STONE_BRICKS -> Blocks.CHISELED_STONE_BRICKS
            Blocks.INFESTED_COBBLESTONE -> Blocks.COBBLESTONE
            Blocks.INFESTED_CRACKED_STONE_BRICKS -> Blocks.CRACKED_STONE_BRICKS
            Blocks.INFESTED_DEEPSLATE -> Blocks.DEEPSLATE
            Blocks.INFESTED_MOSSY_STONE_BRICKS -> Blocks.MOSSY_STONE_BRICKS
            Blocks.INFESTED_STONE -> Blocks.STONE
            Blocks.INFESTED_STONE_BRICKS -> Blocks.STONE_BRICKS
            else -> error("Unhandled block $block")
        }

        DefaultModelFactory.create(actualBlock, fakeBlock, generator, modelGenContext)
    }
}
