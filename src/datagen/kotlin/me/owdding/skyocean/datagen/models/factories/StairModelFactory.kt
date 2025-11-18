package me.owdding.skyocean.datagen.models.factories

import me.owdding.skyocean.datagen.models.BlockModelFactory
import me.owdding.skyocean.datagen.models.ModelGenContext
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.BlockModelGenerators.*
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TexturedModel
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.Half
import net.minecraft.world.level.block.state.properties.StairsShape

object StairModelFactory : BlockModelFactory() {
    private val map = mutableMapOf(
        Blocks.OAK_STAIRS to Blocks.OAK_PLANKS,
        Blocks.SPRUCE_STAIRS to Blocks.SPRUCE_PLANKS,
        Blocks.BIRCH_STAIRS to Blocks.BIRCH_PLANKS,
        Blocks.JUNGLE_STAIRS to Blocks.JUNGLE_PLANKS,
        Blocks.ACACIA_STAIRS to Blocks.ACACIA_PLANKS,
        Blocks.DARK_OAK_STAIRS to Blocks.DARK_OAK_PLANKS,
        Blocks.MANGROVE_STAIRS to Blocks.MANGROVE_PLANKS,
        Blocks.CHERRY_STAIRS to Blocks.CHERRY_PLANKS,
        Blocks.PALE_OAK_STAIRS to Blocks.PALE_OAK_PLANKS,
        Blocks.BAMBOO_STAIRS to Blocks.BAMBOO_PLANKS,
        Blocks.CRIMSON_STAIRS to Blocks.CRIMSON_PLANKS,
        Blocks.WARPED_STAIRS to Blocks.WARPED_PLANKS,
        Blocks.BAMBOO_MOSAIC_STAIRS to Blocks.BAMBOO_MOSAIC,
        Blocks.STONE_STAIRS to Blocks.STONE,
        Blocks.COBBLESTONE_STAIRS to Blocks.COBBLESTONE,
        Blocks.MOSSY_COBBLESTONE_STAIRS to Blocks.MOSSY_COBBLESTONE,
        Blocks.STONE_BRICK_STAIRS to Blocks.STONE_BRICKS,
        Blocks.MOSSY_STONE_BRICK_STAIRS to Blocks.MOSSY_STONE_BRICKS,
        Blocks.GRANITE_STAIRS to Blocks.GRANITE,
        Blocks.POLISHED_GRANITE_STAIRS to Blocks.POLISHED_GRANITE,
        Blocks.DIORITE_STAIRS to Blocks.DIORITE,
        Blocks.POLISHED_DIORITE_STAIRS to Blocks.POLISHED_DIORITE,
        Blocks.ANDESITE_STAIRS to Blocks.ANDESITE,
        Blocks.POLISHED_ANDESITE_STAIRS to Blocks.POLISHED_ANDESITE,
        Blocks.COBBLED_DEEPSLATE_STAIRS to Blocks.COBBLED_DEEPSLATE,
        Blocks.POLISHED_DEEPSLATE_STAIRS to Blocks.POLISHED_DEEPSLATE,
        Blocks.DEEPSLATE_BRICK_STAIRS to Blocks.DEEPSLATE_BRICKS,
        Blocks.DEEPSLATE_TILE_STAIRS to Blocks.DEEPSLATE_TILES,
        Blocks.TUFF_STAIRS to Blocks.TUFF,
        Blocks.POLISHED_TUFF_STAIRS to Blocks.POLISHED_TUFF,
        Blocks.TUFF_BRICK_STAIRS to Blocks.TUFF_BRICKS,
        Blocks.BRICK_STAIRS to Blocks.BRICKS,
        Blocks.MUD_BRICK_STAIRS to Blocks.MUD_BRICKS,
        Blocks.RESIN_BRICK_STAIRS to Blocks.RESIN_BRICKS,
        Blocks.SANDSTONE_STAIRS to Blocks.SANDSTONE,
        Blocks.SMOOTH_SANDSTONE_STAIRS to Blocks.SMOOTH_SANDSTONE,
        Blocks.RED_SANDSTONE_STAIRS to Blocks.RED_SANDSTONE,
        Blocks.SMOOTH_RED_SANDSTONE_STAIRS to Blocks.SMOOTH_RED_SANDSTONE,
        Blocks.PRISMARINE_STAIRS to Blocks.PRISMARINE,
        Blocks.PRISMARINE_BRICK_STAIRS to Blocks.PRISMARINE_BRICKS,
        Blocks.DARK_PRISMARINE_STAIRS to Blocks.DARK_PRISMARINE,
        Blocks.NETHER_BRICK_STAIRS to Blocks.NETHER_BRICKS,
        Blocks.RED_NETHER_BRICK_STAIRS to Blocks.RED_NETHER_BRICKS,
        Blocks.BLACKSTONE_STAIRS to Blocks.BLACKSTONE,
        Blocks.POLISHED_BLACKSTONE_STAIRS to Blocks.POLISHED_BLACKSTONE,
        Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS to Blocks.POLISHED_BLACKSTONE_BRICKS,
        Blocks.END_STONE_BRICK_STAIRS to Blocks.END_STONE_BRICKS,
        Blocks.PURPUR_STAIRS to Blocks.PURPUR_BLOCK,
        Blocks.QUARTZ_STAIRS to Blocks.QUARTZ_BLOCK,
        Blocks.SMOOTH_QUARTZ_STAIRS to Blocks.SMOOTH_QUARTZ,
        Blocks.CUT_COPPER_STAIRS to Blocks.CUT_COPPER,
        Blocks.WAXED_CUT_COPPER_STAIRS to Blocks.WAXED_CUT_COPPER,
        Blocks.EXPOSED_CUT_COPPER_STAIRS to Blocks.EXPOSED_CUT_COPPER,
        Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS to Blocks.WAXED_EXPOSED_CUT_COPPER,
        Blocks.WEATHERED_CUT_COPPER_STAIRS to Blocks.WEATHERED_CUT_COPPER,
        Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS to Blocks.WAXED_WEATHERED_CUT_COPPER,
        Blocks.OXIDIZED_CUT_COPPER_STAIRS to Blocks.OXIDIZED_CUT_COPPER,
        Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS to Blocks.WAXED_OXIDIZED_CUT_COPPER,
    )

    override fun isFor(block: Block): Boolean = map.containsKey(block)
    private fun getBaseBlock(block: Block): Block = map.getOrDefault(block, block)

    override fun create(
        block: Block,
        texture: Block,
        fakeBlock: ResourceLocation,
        parent: ResourceLocation?,
        generator: BlockModelGenerators,
        modelGenContext: ModelGenContext,
    ) {
        val textureMapping = TexturedModel.CUBE.get(getBaseBlock(texture)).mapping

        val innerStairs = ModelTemplates.STAIRS_INNER.plainVariant(fakeBlock, block, textureMapping)
        val straightStairs = ModelTemplates.STAIRS_STRAIGHT.plainVariant(fakeBlock, block, textureMapping)
        val outerStairs = ModelTemplates.STAIRS_OUTER.plainVariant(fakeBlock, block, textureMapping)

        MultiVariantGenerator.dispatch(block)
            .with(
                PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, straightStairs)
                    .select(Direction.WEST, Half.BOTTOM, StairsShape.STRAIGHT, straightStairs.with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.STRAIGHT, straightStairs.with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.NORTH, Half.BOTTOM, StairsShape.STRAIGHT, straightStairs.with(Y_ROT_270).with(UV_LOCK))
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, outerStairs)
                    .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_RIGHT, outerStairs.with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, outerStairs.with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, outerStairs.with(Y_ROT_270).with(UV_LOCK))
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_LEFT, outerStairs.with(Y_ROT_270).with(UV_LOCK))
                    .select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_LEFT, outerStairs.with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, outerStairs)
                    .select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_LEFT, outerStairs.with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, innerStairs)
                    .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_RIGHT, innerStairs.with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_RIGHT, innerStairs.with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_RIGHT, innerStairs.with(Y_ROT_270).with(UV_LOCK))
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_LEFT, innerStairs.with(Y_ROT_270).with(UV_LOCK))
                    .select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_LEFT, innerStairs.with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, innerStairs)
                    .select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_LEFT, innerStairs.with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.EAST, Half.TOP, StairsShape.STRAIGHT, straightStairs.with(X_ROT_180).with(UV_LOCK))
                    .select(Direction.WEST, Half.TOP, StairsShape.STRAIGHT, straightStairs.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.TOP, StairsShape.STRAIGHT, straightStairs.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.NORTH, Half.TOP, StairsShape.STRAIGHT, straightStairs.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK))
                    .select(Direction.EAST, Half.TOP, StairsShape.OUTER_RIGHT, outerStairs.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.WEST, Half.TOP, StairsShape.OUTER_RIGHT, outerStairs.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_RIGHT, outerStairs.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_RIGHT, outerStairs.with(X_ROT_180).with(UV_LOCK))
                    .select(Direction.EAST, Half.TOP, StairsShape.OUTER_LEFT, outerStairs.with(X_ROT_180).with(UV_LOCK))
                    .select(Direction.WEST, Half.TOP, StairsShape.OUTER_LEFT, outerStairs.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_LEFT, outerStairs.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.NORTH, Half.TOP, StairsShape.OUTER_LEFT, outerStairs.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK))
                    .select(Direction.EAST, Half.TOP, StairsShape.INNER_RIGHT, innerStairs.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.WEST, Half.TOP, StairsShape.INNER_RIGHT, innerStairs.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_RIGHT, innerStairs.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.NORTH, Half.TOP, StairsShape.INNER_RIGHT, innerStairs.with(X_ROT_180).with(UV_LOCK))
                    .select(Direction.EAST, Half.TOP, StairsShape.INNER_LEFT, innerStairs.with(X_ROT_180).with(UV_LOCK))
                    .select(Direction.WEST, Half.TOP, StairsShape.INNER_LEFT, innerStairs.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
                    .select(Direction.SOUTH, Half.TOP, StairsShape.INNER_LEFT, innerStairs.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
                    .select(Direction.NORTH, Half.TOP, StairsShape.INNER_LEFT, innerStairs.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK)),
            ).let { modelGenContext.collectState(fakeBlock, it) }
    }
}
