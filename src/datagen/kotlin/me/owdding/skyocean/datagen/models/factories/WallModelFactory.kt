package me.owdding.skyocean.datagen.models.factories

import me.owdding.skyocean.datagen.models.BlockModelFactory
import me.owdding.skyocean.datagen.models.ModelGenContext
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.BlockModelGenerators.*
import net.minecraft.client.data.models.blockstates.MultiPartGenerator
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.WallSide

object WallModelFactory : BlockModelFactory() {
    private val map = mutableMapOf(
        Blocks.COBBLESTONE_WALL to Blocks.COBBLESTONE,
        Blocks.MOSSY_COBBLESTONE_WALL to Blocks.MOSSY_COBBLESTONE,
        Blocks.STONE_BRICK_WALL to Blocks.STONE_BRICKS,
        Blocks.MOSSY_STONE_BRICK_WALL to Blocks.MOSSY_STONE_BRICKS,
        Blocks.GRANITE_WALL to Blocks.GRANITE,
        Blocks.DIORITE_WALL to Blocks.DIORITE,
        Blocks.ANDESITE_WALL to Blocks.ANDESITE,
        Blocks.COBBLED_DEEPSLATE_WALL to Blocks.COBBLED_DEEPSLATE,
        Blocks.POLISHED_DEEPSLATE_WALL to Blocks.POLISHED_DEEPSLATE,
        Blocks.DEEPSLATE_BRICK_WALL to Blocks.DEEPSLATE_BRICKS,
        Blocks.DEEPSLATE_TILE_WALL to Blocks.DEEPSLATE_TILES,
        Blocks.TUFF_WALL to Blocks.TUFF,
        Blocks.POLISHED_TUFF_WALL to Blocks.POLISHED_TUFF,
        Blocks.TUFF_BRICK_WALL to Blocks.TUFF_BRICKS,
        Blocks.BRICK_WALL to Blocks.BRICKS,
        Blocks.MUD_BRICK_WALL to Blocks.MUD_BRICKS,
        Blocks.RESIN_BRICK_WALL to Blocks.RESIN_BRICKS,
        Blocks.SANDSTONE_WALL to Blocks.SANDSTONE,
        Blocks.RED_SANDSTONE_WALL to Blocks.RED_SANDSTONE,
        Blocks.PRISMARINE_WALL to Blocks.PRISMARINE,
        Blocks.NETHER_BRICK_WALL to Blocks.NETHER_BRICKS,
        Blocks.RED_NETHER_BRICK_WALL to Blocks.RED_NETHER_BRICKS,
        Blocks.BLACKSTONE_WALL to Blocks.BLACKSTONE,
        Blocks.POLISHED_BLACKSTONE_WALL to Blocks.POLISHED_BLACKSTONE,
        Blocks.POLISHED_BLACKSTONE_BRICK_WALL to Blocks.POLISHED_BLACKSTONE_BRICKS,
        Blocks.END_STONE_BRICK_WALL to Blocks.END_STONE_BRICKS,
    )

    override fun isFor(block: Block) = map.containsKey(block)

    private fun getBaseBlock(block: Block): Block = map.getOrDefault(block, block)

    override fun create(
        block: Block,
        texture: Block,
        fakeBlock: ResourceLocation,
        parent: ResourceLocation?,
        generator: BlockModelGenerators,
        modelGenContext: ModelGenContext,
    ) {
        val textureMapping = TextureMapping.columnWithWall(getBaseBlock(texture))

        val wallPost = ModelTemplates.WALL_POST.plainVariant(fakeBlock, block, textureMapping)
        val wallLowSide = ModelTemplates.WALL_LOW_SIDE.plainVariant(fakeBlock, block, textureMapping)
        val wallTallSide = ModelTemplates.WALL_TALL_SIDE.plainVariant(fakeBlock, block, textureMapping)

        MultiPartGenerator.multiPart(block)
            .with(condition().term(BlockStateProperties.UP, true), wallPost)
            .with(condition().term(BlockStateProperties.NORTH_WALL, WallSide.LOW), wallLowSide.with(UV_LOCK))
            .with(condition().term(BlockStateProperties.EAST_WALL, WallSide.LOW), wallLowSide.with(Y_ROT_90).with(UV_LOCK))
            .with(condition().term(BlockStateProperties.SOUTH_WALL, WallSide.LOW), wallLowSide.with(Y_ROT_180).with(UV_LOCK))
            .with(condition().term(BlockStateProperties.WEST_WALL, WallSide.LOW), wallLowSide.with(Y_ROT_270).with(UV_LOCK))
            .with(condition().term(BlockStateProperties.NORTH_WALL, WallSide.TALL), wallTallSide.with(UV_LOCK))
            .with(condition().term(BlockStateProperties.EAST_WALL, WallSide.TALL), wallTallSide.with(Y_ROT_90).with(UV_LOCK))
            .with(condition().term(BlockStateProperties.SOUTH_WALL, WallSide.TALL), wallTallSide.with(Y_ROT_180).with(UV_LOCK))
            .with(condition().term(BlockStateProperties.WEST_WALL, WallSide.TALL), wallTallSide.with(Y_ROT_270).with(UV_LOCK))
            .let { modelGenContext.collectState(fakeBlock, it) }
    }
}
