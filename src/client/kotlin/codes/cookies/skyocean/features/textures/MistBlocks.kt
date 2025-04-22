package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.config.features.mining.MiningRetexture
import codes.cookies.skyocean.events.RegisterFakeBlocksEvent
import codes.cookies.skyocean.utils.boundingboxes.DwarvenMinesBB
import me.owdding.ktmodules.Module
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object MistBlocks : BlockRetexture() {

    @Subscription
    fun registerFakeBlocks(event: RegisterFakeBlocksEvent) = with(event) {
        register(Blocks.SNOW, "mist_snow")
        register(Blocks.SNOW_BLOCK, "mist_snow_block")
        register(Blocks.WHITE_STAINED_GLASS, "mist_glass")
        register(Blocks.CLAY, "mist_clay")
        register(Blocks.ICE, "mist_ice")
        register(Blocks.LIGHT_BLUE_STAINED_GLASS, "mist_glass_secondary")
        register(Blocks.WHITE_CARPET, "mist_carpet")
        register(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, "mist_light_blue_glass_pane")
        register(Blocks.BLUE_STAINED_GLASS_PANE, "mist_blue_glass_pane")
    }

    override fun defaultCondition(blockState: BlockState, blockPos: BlockPos): Boolean {
        if (!MiningRetexture.customMist) return false

        if (!SkyBlockIsland.DWARVEN_MINES.inIsland()) return false
        return DwarvenMinesBB.MIST.isInside(blockPos)
    }
}
