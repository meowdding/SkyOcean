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
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object MistBlocks : BlockRetexture() {

    val MIST_SNOW = mist("mist_snow")
    val MIST_SNOW_BLOCK = mist("mist_snow_block")
    val MIST_GLASS = mist("mist_glass")
    val MIST_GLASS_SECONDARY = mist("mist_glass_secondary")
    val MIST_CLAY = mist("mist_clay")
    val MIST_ICE = mist("mist_ice")
    val MIST_CARPET = mist("mist_carpet")
    val MIST_LIGHT_BLUE_GLASS_PANE = mist("mist_light_blue_glass_pane")
    val MIST_BLUE_GLASS_PANE = mist("mist_blue_glass_pane")

    private fun mist(s: String) = id("mining/mist/$s")

    @Subscription
    fun registerFakeBlocks(event: RegisterFakeBlocksEvent) = with(event) {
        register(Blocks.SNOW, MIST_SNOW)
        register(Blocks.SNOW_BLOCK, MIST_SNOW_BLOCK)
        register(Blocks.WHITE_STAINED_GLASS, MIST_GLASS)
        register(Blocks.CLAY, MIST_CLAY)
        register(Blocks.ICE, MIST_ICE)
        register(Blocks.LIGHT_BLUE_STAINED_GLASS, MIST_GLASS_SECONDARY)
        register(Blocks.WHITE_CARPET, MIST_CARPET)
        register(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, MIST_LIGHT_BLUE_GLASS_PANE)
        register(Blocks.BLUE_STAINED_GLASS_PANE, MIST_BLUE_GLASS_PANE)
    }

    override fun defaultCondition(blockState: BlockState, blockPos: BlockPos): Boolean {
        if (!MiningRetexture.customMist) return false

        if (!SkyBlockIsland.DWARVEN_MINES.inIsland()) return false
        return DwarvenMinesBB.MIST.isInside(blockPos)
    }
}
