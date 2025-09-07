package me.owdding.skyocean.events

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.events.base.CancellableSkyBlockEvent

typealias FakeBlockModelEventRegistrar = (
    block: Block,
    texture: Block,
    definition: ResourceLocation,
    parent: ResourceLocation?,
    predicate: (BlockState, BlockPos) -> Boolean,
) -> Unit

data class RegisterFakeBlocksEvent(private val registrar: FakeBlockModelEventRegistrar) : CancellableSkyBlockEvent() {

    fun register(
        block: Block,
        definition: ResourceLocation,
        parent: ResourceLocation?,
        predicate: (BlockState, BlockPos) -> Boolean,
    ) = registrar(block, block, definition, parent, predicate)

}

