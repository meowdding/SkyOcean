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
    val GLACITE_HARD_STONE_CAMPFIRE_MINESHAFT = id("mining/glacite/glacite_hard_stone_campfire")
    val GLACITE_HARD_STONE_WOOL = id("mining/glacite/glacite_hard_stone_wool")
    val LOW_TIER_TUNGSTEN = id("mining/glacite/low_tier_tungsten")
    val LOW_TIER_TUNGSTEN_MINESHAFT = id("mining/glacite/low_tier_tungsten_mineshaft")
    val LOW_TIER_TUNGSTEN_STAIRS = id("mining/glacite/low_tier_tungsten_stairs")
    val LOW_TIER_TUNGSTEN_SLAB = id("mining/glacite/low_tier_tungsten_slab")
    val HIGH_TIER_TUNGSTEN = id("mining/glacite/high_tier_tungsten")
    val LOW_TIER_UMBER = id("mining/glacite/low_tier_umber")
    val MID_TIER_UMBER = id("mining/glacite/mid_tier_umber")
    val HIGH_TIER_UMBER = id("mining/glacite/high_tier_umber")


    @Subscription
    fun registerFakeBlocks(event: RegisterFakeBlocksEvent) = with(event) {
        register(Blocks.SNOW, GLACITE_SNOW)
        register(Blocks.SNOW_BLOCK, GLACITE_SNOW_BLOCK)
        register(Blocks.PACKED_ICE, GLACITE_BLOCK)
        register(Blocks.INFESTED_STONE, id = GLACITE_HARD_STONE)
        register(Blocks.STONE, GLACITE_HARD_STONE_CAMPFIRE_MINESHAFT, GLACITE_HARD_STONE)
        register(Blocks.LIGHT_GRAY_WOOL, GLACITE_HARD_STONE_WOOL, CrystalHollowBlocks.HARDSTONE)
        register(Blocks.INFESTED_COBBLESTONE, LOW_TIER_TUNGSTEN)
        register(Blocks.COBBLESTONE, id = LOW_TIER_TUNGSTEN_MINESHAFT, LOW_TIER_TUNGSTEN) { state, pos ->
            if (state.block == Blocks.COBBLESTONE && !SkyBlockIsland.MINESHAFT.inIsland()) return@register false
            return@register defaultCondition(state, pos)
        }
        register(Blocks.COBBLESTONE_STAIRS, id = LOW_TIER_TUNGSTEN_STAIRS) { state, pos ->
            if (state.block == Blocks.COBBLESTONE_STAIRS && !SkyBlockIsland.MINESHAFT.inIsland()) return@register false
            return@register defaultCondition(state, pos)
        }
        register(Blocks.COBBLESTONE_SLAB, id = LOW_TIER_TUNGSTEN_SLAB) { state, pos ->
            if (state.block == Blocks.COBBLESTONE_SLAB && !SkyBlockIsland.MINESHAFT.inIsland()) return@register false
            return@register defaultCondition(state, pos)
        }
        register(Blocks.CLAY, HIGH_TIER_TUNGSTEN)
        register(Blocks.TERRACOTTA, LOW_TIER_UMBER)
        register(Blocks.BROWN_TERRACOTTA, MID_TIER_UMBER)
        register(Blocks.SMOOTH_RED_SANDSTONE, HIGH_TIER_UMBER)
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
