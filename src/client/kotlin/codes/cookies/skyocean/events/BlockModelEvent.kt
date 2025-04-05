package codes.cookies.skyocean.events

import net.minecraft.client.renderer.block.model.BlockStateModel
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.events.base.CancellableSkyBlockEvent

data class BlockModelEvent(var state: BlockState, val modelByStateCache: Map<BlockState, BlockStateModel>): CancellableSkyBlockEvent() {
    var model: BlockStateModel? = null
}
