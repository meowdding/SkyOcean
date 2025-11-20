package me.owdding.skyocean.datagen.models.factories

import me.owdding.skyocean.datagen.models.BlockModelFactory
import me.owdding.skyocean.datagen.models.ModelGenContext
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TexturedModel
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.SlabType

object SlabModelFactory : BlockModelFactory() {
    private val map = mutableMapOf(
        Blocks.RESIN_BRICK_SLAB to Blocks.RESIN_BRICKS,
        Blocks.PRISMARINE_SLAB to Blocks.PRISMARINE,
        Blocks.PRISMARINE_BRICK_SLAB to Blocks.PRISMARINE_BRICKS,
        Blocks.DARK_PRISMARINE_SLAB to Blocks.DARK_PRISMARINE,
        Blocks.OAK_SLAB to Blocks.OAK_PLANKS,
        Blocks.SPRUCE_SLAB to Blocks.SPRUCE_PLANKS,
        Blocks.BIRCH_SLAB to Blocks.BIRCH_PLANKS,
        Blocks.JUNGLE_SLAB to Blocks.JUNGLE_PLANKS,
        Blocks.ACACIA_SLAB to Blocks.ACACIA_PLANKS,
        Blocks.CHERRY_SLAB to Blocks.CHERRY_PLANKS,
        Blocks.DARK_OAK_SLAB to Blocks.DARK_OAK_PLANKS,
        Blocks.PALE_OAK_SLAB to Blocks.PALE_OAK_PLANKS,
        Blocks.MANGROVE_SLAB to Blocks.MANGROVE_PLANKS,
        Blocks.BAMBOO_SLAB to Blocks.BAMBOO_PLANKS,
        Blocks.BAMBOO_MOSAIC_SLAB to Blocks.BAMBOO_MOSAIC,
        Blocks.STONE_SLAB to Blocks.STONE,
        Blocks.SMOOTH_STONE_SLAB to Blocks.SMOOTH_STONE,
        Blocks.SANDSTONE_SLAB to Blocks.SANDSTONE,
        Blocks.CUT_SANDSTONE_SLAB to Blocks.CUT_SANDSTONE,
        Blocks.PETRIFIED_OAK_SLAB to Blocks.OAK_PLANKS,
        Blocks.COBBLESTONE_SLAB to Blocks.COBBLESTONE,
        Blocks.BRICK_SLAB to Blocks.BRICKS,
        Blocks.STONE_BRICK_SLAB to Blocks.STONE_BRICKS,
        Blocks.MUD_BRICK_SLAB to Blocks.MUD_BRICKS,
        Blocks.NETHER_BRICK_SLAB to Blocks.NETHER_BRICKS,
        Blocks.QUARTZ_SLAB to Blocks.QUARTZ_BLOCK,
        Blocks.RED_SANDSTONE_SLAB to Blocks.RED_SANDSTONE,
        Blocks.CUT_RED_SANDSTONE_SLAB to Blocks.CUT_RED_SANDSTONE,
        Blocks.PURPUR_SLAB to Blocks.PURPUR_BLOCK,
        Blocks.POLISHED_GRANITE_SLAB to Blocks.POLISHED_GRANITE,
        Blocks.SMOOTH_RED_SANDSTONE_SLAB to Blocks.SMOOTH_RED_SANDSTONE,
        Blocks.MOSSY_STONE_BRICK_SLAB to Blocks.MOSSY_STONE_BRICKS,
        Blocks.POLISHED_DIORITE_SLAB to Blocks.POLISHED_DIORITE,
        Blocks.MOSSY_COBBLESTONE_SLAB to Blocks.MOSSY_COBBLESTONE,
        Blocks.END_STONE_BRICK_SLAB to Blocks.END_STONE_BRICKS,
        Blocks.SMOOTH_SANDSTONE_SLAB to Blocks.SMOOTH_SANDSTONE,
        Blocks.SMOOTH_QUARTZ_SLAB to Blocks.SMOOTH_QUARTZ,
        Blocks.GRANITE_SLAB to Blocks.GRANITE,
        Blocks.ANDESITE_SLAB to Blocks.ANDESITE,
        Blocks.RED_NETHER_BRICK_SLAB to Blocks.RED_NETHER_BRICKS,
        Blocks.POLISHED_ANDESITE_SLAB to Blocks.POLISHED_ANDESITE,
        Blocks.DIORITE_SLAB to Blocks.DIORITE,
        Blocks.CRIMSON_SLAB to Blocks.CRIMSON_PLANKS,
        Blocks.WARPED_SLAB to Blocks.WARPED_PLANKS,
        Blocks.BLACKSTONE_SLAB to Blocks.BLACKSTONE,
        Blocks.POLISHED_BLACKSTONE_BRICK_SLAB to Blocks.POLISHED_BLACKSTONE_BRICKS,
        Blocks.POLISHED_BLACKSTONE_SLAB to Blocks.POLISHED_BLACKSTONE,
        Blocks.TUFF_SLAB to Blocks.TUFF,
        Blocks.POLISHED_TUFF_SLAB to Blocks.POLISHED_TUFF,
        Blocks.TUFF_BRICK_SLAB to Blocks.TUFF_BRICKS,
        Blocks.OXIDIZED_CUT_COPPER_SLAB to Blocks.OXIDIZED_CUT_COPPER,
        Blocks.WEATHERED_CUT_COPPER_SLAB to Blocks.WEATHERED_CUT_COPPER,
        Blocks.EXPOSED_CUT_COPPER_SLAB to Blocks.EXPOSED_CUT_COPPER,
        Blocks.CUT_COPPER_SLAB to Blocks.CUT_COPPER,
        Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB to Blocks.WAXED_OXIDIZED_CUT_COPPER,
        Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB to Blocks.WAXED_WEATHERED_CUT_COPPER,
        Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB to Blocks.WAXED_EXPOSED_CUT_COPPER,
        Blocks.WAXED_CUT_COPPER_SLAB to Blocks.WAXED_CUT_COPPER,
        Blocks.COBBLED_DEEPSLATE_SLAB to Blocks.COBBLED_DEEPSLATE,
        Blocks.POLISHED_DEEPSLATE_SLAB to Blocks.POLISHED_DEEPSLATE,
        Blocks.DEEPSLATE_TILE_SLAB to Blocks.DEEPSLATE_TILES,
        Blocks.DEEPSLATE_BRICK_SLAB to Blocks.DEEPSLATE_BRICKS,
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

        val bottomSlab = ModelTemplates.SLAB_BOTTOM.plainVariant(fakeBlock, block, textureMapping)
        val topSlab = ModelTemplates.SLAB_TOP.plainVariant(fakeBlock, block, textureMapping)
        val doubleSlab = ModelTemplates.CUBE_ALL.plainVariant(fakeBlock.withSuffix("_double"), getBaseBlock(block), textureMapping)

        MultiVariantGenerator.dispatch(block)
            .with(
                PropertyDispatch.initial(BlockStateProperties.SLAB_TYPE)
                    .select(SlabType.BOTTOM, bottomSlab)
                    .select(SlabType.TOP, topSlab)
                    .select(SlabType.DOUBLE, doubleSlab),
            ).let { modelGenContext.collectState(fakeBlock, it) }
    }
}
