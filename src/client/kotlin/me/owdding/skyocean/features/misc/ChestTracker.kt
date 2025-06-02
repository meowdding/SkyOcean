package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.Utils.plus
import me.owdding.skyocean.utils.tags.BlockTagKey
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.DoubleBlockCombiner
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.ChestType
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.level.BlockChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.RightClickBlockEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McLevel

@Module
object ChestTracker {
    private var first: BlockPos? = null
    private var second: BlockPos? = null

    fun resetCoords() {
        first = null
        second = null
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onBlockInteract(event: RightClickBlockEvent) {
        val blockState = McLevel[event.pos]
        if (blockState !in BlockTagKey.CHESTS) return
        val chestType = blockState.getValue(BlockStateProperties.CHEST_TYPE)
        val type = getType(chestType)
        first = event.pos
        if (chestType !== ChestType.SINGLE) {
            var diff = when (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction.EAST -> BlockPos(0, 0, 1)
                Direction.WEST -> BlockPos(0, 0, -1)
                Direction.NORTH -> BlockPos(1, 0, 0)
                Direction.SOUTH -> BlockPos(-1, 0, 0)
                else -> BlockPos.ZERO
            }

            if (chestType !== ChestType.LEFT) {
                diff = diff.multiply(-1)
            }
            val doubleChestPosition: BlockPos? = this.first!!.plus(diff)
            if (type == DoubleBlockCombiner.BlockType.FIRST) {
                this.second = doubleChestPosition
            } else {
                this.second = this.first
                this.first = doubleChestPosition
            }
        }

    }

    @Subscription
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onBlockBreak(event: BlockChangeEvent) {

    }

    @Subscription
    fun ContainerCloseEvent.onInventoryClose() {

    }

    fun getType(chestType: ChestType): DoubleBlockCombiner.BlockType = when (chestType) {
        ChestType.SINGLE -> DoubleBlockCombiner.BlockType.SINGLE
        ChestType.RIGHT -> DoubleBlockCombiner.BlockType.FIRST
        else -> DoubleBlockCombiner.BlockType.SECOND
    }

}
