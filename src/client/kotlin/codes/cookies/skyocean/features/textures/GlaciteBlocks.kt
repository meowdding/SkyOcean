package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.events.RegisterFakeBlocksEvent
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.boundingboxes.DwarvenMinesBB
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object GlaciteBlocks : BlockRetexture() {

    @Subscription
    fun registerFakeBlocks(event: RegisterFakeBlocksEvent) = with(event) {
        register(Blocks.SNOW, "glacite_snow")
        register(Blocks.SNOW_BLOCK, "glacite_snow_block")
        register(Blocks.PACKED_ICE, "glacite")
        register(Blocks.INFESTED_STONE, "glacite_hard_stone")
        register(Blocks.LIGHT_GRAY_WOOL, "glacite_hard_stone_wool")
    }

    override fun defaultCondition(blockState: BlockState, blockPos: BlockPos): Boolean {
        if (!SkyBlockIsland.DWARVEN_MINES.inIsland()) return false
        return DwarvenMinesBB.GLACITE_TUNNELS.isInside(blockPos)
    }

}
