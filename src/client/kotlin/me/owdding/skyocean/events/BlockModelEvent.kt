package me.owdding.skyocean.events

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.events.base.CancellableSkyBlockEvent

data class BlockModelEvent(var state: BlockState) : CancellableSkyBlockEvent() {
    var block: Block
        get() = state.block
        set(value) {
            state = value.defaultBlockState()
        }
}
