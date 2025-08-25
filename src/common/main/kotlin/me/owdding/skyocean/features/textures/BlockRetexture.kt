package me.owdding.skyocean.features.textures

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.events.RegisterFakeBlocksEvent
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

typealias Condition = (BlockState, BlockPos) -> Boolean

abstract class BlockRetexture {
    val map = mutableMapOf<Block, Block>()

    abstract fun defaultCondition(blockState: BlockState, blockPos: BlockPos): Boolean

    fun RegisterFakeBlocksEvent.register(block: Block, id: String, parent: ResourceLocation? = null, condition: Condition = ::defaultCondition) {
        this.register(block, SkyOcean.id(id), parent, condition)
    }

    fun RegisterFakeBlocksEvent.register(block: Block, id: ResourceLocation, parent: ResourceLocation? = null, condition: Condition = ::defaultCondition) {
        this.register(block, id, parent, condition)
    }

    fun RegisterFakeBlocksEvent.registerMultiple(
        vararg defaultBlocks: Block,
        id: String,
        parent: ResourceLocation? = null,
        condition: Condition = ::defaultCondition,
    ) {
        defaultBlocks.forEach { this.register(it, id, null, condition) }
    }

    fun RegisterFakeBlocksEvent.registerMultiple(
        vararg defaultBlocks: Block,
        id: ResourceLocation,
        parent: ResourceLocation? = null,
        condition: Condition = ::defaultCondition,
    ) {
        defaultBlocks.forEach { this.register(it, id, parent, condition) }
    }
}
