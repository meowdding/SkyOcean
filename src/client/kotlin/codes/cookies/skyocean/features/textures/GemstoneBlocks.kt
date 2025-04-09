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
        register(Blocks.WHITE_STAINED_GLASS, "opal")
        register(Blocks.WHITE_STAINED_GLASS_PANE, "opal_pane")
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

    override fun defaultCondition(blockState: BlockState, blockPos: BlockPos): Boolean {
        if (!SkyBlockIsland.DWARVEN_MINES.inIsland()) return false
        return DwarvenMinesBB.GEMSTONE_LOCATIONS.isInside(blockPos)
    }
}
