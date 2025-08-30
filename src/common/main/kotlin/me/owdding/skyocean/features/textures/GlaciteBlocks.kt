package me.owdding.skyocean.features.textures

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.config.features.mining.MiningRetexture
import me.owdding.skyocean.events.RegisterFakeBlocksEvent
import me.owdding.skyocean.utils.boundingboxes.DwarvenMinesBB
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object GlaciteBlocks : BlockRetexture() {

    val GLACITE_BLOCK = id("mining/glacite/glacite_block")
    val GLACITE_SNOW = id("mining/glacite/glacite_snow")
    val GLACITE_SNOW_BLOCK = id("mining/glacite/glacite_snow_block")
    val GLACITE_HARD_STONE = id("mining/glacite/glacite_hard_stone")
    val GLACITE_HARD_STONE_WOOL = id("glacite/glacite_hard_stone_wool")

    @Subscription
    fun registerFakeBlocks(event: RegisterFakeBlocksEvent) = with(event) {
        register(Blocks.SNOW, GLACITE_SNOW)
        register(Blocks.SNOW_BLOCK, GLACITE_SNOW_BLOCK)
        register(Blocks.PACKED_ICE, GLACITE_BLOCK)
        registerMultiple(Blocks.INFESTED_STONE, Blocks.STONE, id = GLACITE_HARD_STONE, parent = CrystalHollowBlocks.HARDSTONE) { state, pos ->
            if (state.block == Blocks.STONE && !SkyBlockIsland.MINESHAFT.inIsland()) return@registerMultiple false
            return@registerMultiple defaultCondition(state, pos)
        }
        register(Blocks.LIGHT_GRAY_WOOL, GLACITE_HARD_STONE_WOOL, CrystalHollowBlocks.HARDSTONE)
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
