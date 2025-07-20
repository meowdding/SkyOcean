package me.owdding.skyocean.features.textures

import me.owdding.ktmodules.Module
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
object GemstoneBlocks : BlockRetexture() {

    @Subscription
    fun registerFakeBlocks(event: RegisterFakeBlocksEvent) = with(event) {
        register(Blocks.RED_STAINED_GLASS, "ruby")
        register(Blocks.RED_STAINED_GLASS_PANE, "ruby_pane")
        register(Blocks.ORANGE_STAINED_GLASS, "amber")
        register(Blocks.ORANGE_STAINED_GLASS_PANE, "amber_pane")
        register(Blocks.LIGHT_BLUE_STAINED_GLASS, "sapphire")
        register(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, "sapphire_pane")
        register(Blocks.LIME_STAINED_GLASS, "jade")
        register(Blocks.LIME_STAINED_GLASS_PANE, "jade_pane")
        register(Blocks.PURPLE_STAINED_GLASS, "amethyst")
        register(Blocks.PURPLE_STAINED_GLASS_PANE, "amethyst_pane")
        register(Blocks.WHITE_STAINED_GLASS, "opal", ::opalCondition) // opal can only generate in mineshafts and crimson isle
        register(Blocks.WHITE_STAINED_GLASS_PANE, "opal_pane", ::opalCondition) // opal can only generate in mineshafts and crimson isle
        register(Blocks.YELLOW_STAINED_GLASS, "topaz")
        register(Blocks.YELLOW_STAINED_GLASS_PANE, "topaz_pane")
        register(Blocks.MAGENTA_STAINED_GLASS, "jasper")
        register(Blocks.MAGENTA_STAINED_GLASS_PANE, "jasper_pane")
        register(Blocks.BLACK_STAINED_GLASS, "onyx")
        register(Blocks.BLACK_STAINED_GLASS_PANE, "onyx_pane")
        register(Blocks.BLUE_STAINED_GLASS, "aquamarine")
        register(Blocks.BLUE_STAINED_GLASS_PANE, "aquamarine_pane")
        register(Blocks.BROWN_STAINED_GLASS, "citrine")
        register(Blocks.BROWN_STAINED_GLASS_PANE, "citrine_pane")
        register(Blocks.GREEN_STAINED_GLASS, "peridot")
        register(Blocks.GREEN_STAINED_GLASS_PANE, "peridot_pane")
    }

    fun opalCondition(blockState: BlockState, blockPos: BlockPos): Boolean {
        if (SkyBlockIsland.inAnyIsland(SkyBlockIsland.CRYSTAL_HOLLOWS, SkyBlockIsland.DWARVEN_MINES)) {
            return false
        }
        return defaultCondition(blockState, blockPos)
    }

    override fun defaultCondition(blockState: BlockState, blockPos: BlockPos): Boolean {
        if (!MiningRetexture.customGemstoneTextures) return false

        return when (LocationAPI.island) {
            SkyBlockIsland.DWARVEN_MINES -> DwarvenMinesBB.GEMSTONE_LOCATIONS.isInside(blockPos)
            SkyBlockIsland.MINESHAFT, SkyBlockIsland.CRYSTAL_HOLLOWS -> true
            else -> false
        }
    }
}
