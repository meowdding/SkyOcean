package me.owdding.skyocean.features.textures

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.events.RegisterFakeBlocksEvent
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object CrystalHollowBlocks : BlockRetexture() {

    val HARDSTONE = id("mining/hollows/hardstone")
    val COAL_ORE = id("mining/hollows/coal_ore")
    val IRON_ORE = id("mining/hollows/iron_ore")
    val EMERALD_ORE = id("mining/hollows/emerald_ore")
    val GOLD_ORE = id("mining/hollows/gold_ore")
    val DIAMOND_ORE = id("mining/hollows/diamond_ore")
    val REDSTONE_ORE = id("mining/hollows/redstone_ore")

    @Subscription
    fun registerFakeBlocks(event: RegisterFakeBlocksEvent) = with(event) {
        register(Blocks.STONE, HARDSTONE)
        register(Blocks.COAL_ORE, COAL_ORE)
        register(Blocks.IRON_ORE, IRON_ORE)
        register(Blocks.EMERALD_ORE, EMERALD_ORE)
        register(Blocks.GOLD_BLOCK, GOLD_ORE)
        register(Blocks.DIAMOND_ORE, DIAMOND_ORE)
        register(Blocks.REDSTONE_ORE, REDSTONE_ORE)
    }

    override fun defaultCondition(blockState: BlockState, blockPos: BlockPos) = SkyBlockIsland.CRYSTAL_HOLLOWS.inIsland()
}
