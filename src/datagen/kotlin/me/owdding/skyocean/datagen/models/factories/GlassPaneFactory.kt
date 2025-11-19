package me.owdding.skyocean.datagen.models.factories

import me.owdding.skyocean.datagen.models.BlockModelFactory
import me.owdding.skyocean.datagen.models.ModelGenContext
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_270
import net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_90
import net.minecraft.client.data.models.blockstates.MultiPartGenerator
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.StainedGlassPaneBlock
import net.minecraft.world.level.block.state.properties.BlockStateProperties

object GlassPaneFactory : BlockModelFactory() {
    override fun isFor(block: Block) = block is StainedGlassPaneBlock || block == Blocks.GLASS_PANE

    private fun getBaseGlassBlock(block: Block) = when (block) {
        Blocks.GLASS_PANE -> Blocks.GLASS
        Blocks.BLACK_STAINED_GLASS_PANE -> Blocks.BLACK_STAINED_GLASS
        Blocks.BLUE_STAINED_GLASS_PANE -> Blocks.BLUE_STAINED_GLASS
        Blocks.BROWN_STAINED_GLASS_PANE -> Blocks.BROWN_STAINED_GLASS
        Blocks.CYAN_STAINED_GLASS_PANE -> Blocks.CYAN_STAINED_GLASS
        Blocks.GRAY_STAINED_GLASS_PANE -> Blocks.GRAY_STAINED_GLASS
        Blocks.GREEN_STAINED_GLASS_PANE -> Blocks.GREEN_STAINED_GLASS
        Blocks.LIGHT_BLUE_STAINED_GLASS_PANE -> Blocks.LIGHT_BLUE_STAINED_GLASS
        Blocks.LIGHT_GRAY_STAINED_GLASS_PANE -> Blocks.LIGHT_GRAY_STAINED_GLASS
        Blocks.LIME_STAINED_GLASS_PANE -> Blocks.LIME_STAINED_GLASS
        Blocks.MAGENTA_STAINED_GLASS_PANE -> Blocks.MAGENTA_STAINED_GLASS
        Blocks.ORANGE_STAINED_GLASS_PANE -> Blocks.ORANGE_STAINED_GLASS
        Blocks.PINK_STAINED_GLASS_PANE -> Blocks.PINK_STAINED_GLASS
        Blocks.PURPLE_STAINED_GLASS_PANE -> Blocks.PURPLE_STAINED_GLASS
        Blocks.RED_STAINED_GLASS_PANE -> Blocks.RED_STAINED_GLASS
        Blocks.WHITE_STAINED_GLASS_PANE -> Blocks.WHITE_STAINED_GLASS
        Blocks.YELLOW_STAINED_GLASS_PANE -> Blocks.YELLOW_STAINED_GLASS
        else -> block
    }

    override fun create(
        block: Block,
        texture: Block,
        fakeBlock: ResourceLocation,
        parent: ResourceLocation?,
        generator: BlockModelGenerators,
        modelGenContext: ModelGenContext,
    ) {
        assert(parent == null) { "Parents aren't supported for MultiPart blocks" }
        val textureMapping = TextureMapping.pane(getBaseGlassBlock(texture), texture)
        val post = ModelTemplates.STAINED_GLASS_PANE_POST.plainVariant(fakeBlock, block, textureMapping)
        val side = ModelTemplates.STAINED_GLASS_PANE_SIDE.plainVariant(fakeBlock, block, textureMapping)
        val sideAlt = ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.plainVariant(fakeBlock, block, textureMapping)
        val noSide = ModelTemplates.STAINED_GLASS_PANE_NOSIDE.plainVariant(fakeBlock, block, textureMapping)
        val noSideAlt = ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.plainVariant(fakeBlock, block, textureMapping)
        MultiPartGenerator.multiPart(block).with(post)
            .with(BlockModelGenerators.condition().term(BlockStateProperties.NORTH, true), side)
            .with(BlockModelGenerators.condition().term(BlockStateProperties.EAST, true), side.with(Y_ROT_90))
            .with(BlockModelGenerators.condition().term(BlockStateProperties.SOUTH, true), sideAlt)
            .with(BlockModelGenerators.condition().term(BlockStateProperties.WEST, true), sideAlt.with(Y_ROT_90))
            .with(BlockModelGenerators.condition().term(BlockStateProperties.NORTH, false), noSide)
            .with(BlockModelGenerators.condition().term(BlockStateProperties.EAST, false), noSideAlt)
            .with(BlockModelGenerators.condition().term(BlockStateProperties.SOUTH, false), noSideAlt.with(Y_ROT_90))
            .with(BlockModelGenerators.condition().term(BlockStateProperties.WEST, false), noSide.with(Y_ROT_270))
            .let { modelGenContext.collectState(fakeBlock, it) }
    }
}
