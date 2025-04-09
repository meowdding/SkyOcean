package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.SkyOcean
import codes.cookies.skyocean.events.RegisterFakeBlocksEvent
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

typealias Condition = (BlockState, BlockPos) -> Boolean

abstract class BlockRetexture {
    val map = mutableMapOf<Block, Block>()

    abstract fun defaultCondition(blockState: BlockState, blockPos: BlockPos): Boolean

    fun RegisterFakeBlocksEvent.register(block: Block, id: String, condition: Condition = ::defaultCondition) {
        this.register(block, SkyOcean.id(id), condition)
    }

    fun RegisterFakeBlocksEvent.registerMultiple(vararg defaultBlocks: Block, id: String, condition: Condition = ::defaultCondition){
        defaultBlocks.forEach { this.register(it, id, condition) }
    }
}
