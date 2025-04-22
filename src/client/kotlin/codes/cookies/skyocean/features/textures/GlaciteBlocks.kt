package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.config.features.mining.MiningRetexture
import codes.cookies.skyocean.events.RegisterFakeBlocksEvent
import codes.cookies.skyocean.utils.boundingboxes.DwarvenMinesBB
import me.owdding.ktmodules.Module
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object GlaciteBlocks : BlockRetexture() {

    @Subscription
    fun registerFakeBlocks(event: RegisterFakeBlocksEvent) = with(event) {
        register(Blocks.SNOW, "glacite_snow")
        register(Blocks.SNOW_BLOCK, "glacite_snow_block")
        register(Blocks.PACKED_ICE, "glacite")
        registerMultiple(Blocks.INFESTED_STONE, Blocks.STONE, id = "glacite_hard_stone") { state, pos ->
            if (state.block == Blocks.STONE && !SkyBlockIsland.MINESHAFT.inIsland()) return@registerMultiple false
            return@registerMultiple defaultCondition(state, pos)
        }
        register(Blocks.LIGHT_GRAY_WOOL, "glacite_hard_stone_wool")
    }

    override fun defaultCondition(blockState: BlockState, blockPos: BlockPos): Boolean {
        if (!MiningRetexture.customGlaciteTextures) return false

        return when (LocationAPI.island) {
            SkyBlockIsland.DWARVEN_MINES -> DwarvenMinesBB.GLACITE_TUNNELS.isInside(blockPos)
            SkyBlockIsland.MINESHAFT -> true
            else -> false
        }
    }

}
