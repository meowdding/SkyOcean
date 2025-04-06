package codes.cookies.skyocean.events

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.events.base.CancellableSkyBlockEvent

data class BlockModelEvent(var state: BlockState, val pos: BlockPos) : CancellableSkyBlockEvent() {
    var block: Block
        get() = state.block
        set(value) {
            state = value.defaultBlockState()
        }
}
