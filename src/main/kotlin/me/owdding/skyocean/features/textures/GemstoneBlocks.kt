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
object GemstoneBlocks : BlockRetexture() {

    val RUBY = gemstones("ruby")
    val RUBY_PANE = gemstones("ruby_pane")
    val AMBER = gemstones("amber")
    val AMBER_PANE = gemstones("amber_pane")
    val SAPPHIRE = gemstones("sapphire")
    val SAPPHIRE_PANE = gemstones("sapphire_pane")
    val JADE = gemstones("jade")
    val JADE_PANE = gemstones("jade_pane")
    val AMETHYST = gemstones("amethyst")
    val AMETHYST_PANE = gemstones("amethyst_pane")
    val OPAL = gemstones("opal")
    val OPAL_PANE = gemstones("opal_pane")
    val TOPAZ = gemstones("topaz")
    val TOPAZ_PANE = gemstones("topaz_pane")
    val JASPER = gemstones("jasper")
    val JASPER_PANE = gemstones("jasper_pane")
    val ONYX = gemstones("onyx")
    val ONYX_PANE = gemstones("onyx_pane")
    val AQUAMARINE = gemstones("aquamarine")
    val AQUAMARINE_PANE = gemstones("aquamarine_pane")
    val CITRINE = gemstones("citrine")
    val CITRINE_PANE = gemstones("citrine_pane")
    val PERIDOT = gemstones("peridot")
    val PERIDOT_PANE = gemstones("peridot_pane")

    private fun gemstones(s: String) = id("mining/gemstones/$s")

    @Subscription
    fun registerFakeBlocks(event: RegisterFakeBlocksEvent) = with(event) {
        register(Blocks.RED_STAINED_GLASS, RUBY)
        register(Blocks.RED_STAINED_GLASS_PANE, RUBY_PANE)
        register(Blocks.ORANGE_STAINED_GLASS, AMBER)
        register(Blocks.ORANGE_STAINED_GLASS_PANE, AMBER_PANE)
        register(Blocks.LIGHT_BLUE_STAINED_GLASS, SAPPHIRE)
        register(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, SAPPHIRE_PANE)
        register(Blocks.LIME_STAINED_GLASS, JADE)
        register(Blocks.LIME_STAINED_GLASS_PANE, JADE_PANE)
        register(Blocks.PURPLE_STAINED_GLASS, AMETHYST)
        register(Blocks.PURPLE_STAINED_GLASS_PANE, AMETHYST_PANE)
        register(Blocks.WHITE_STAINED_GLASS, OPAL, condition = ::opalCondition) // opal can only generate in mineshafts and crimson isle
        register(Blocks.WHITE_STAINED_GLASS_PANE, OPAL_PANE, condition = ::opalCondition) // opal can only generate in mineshafts and crimson isle
        register(Blocks.YELLOW_STAINED_GLASS, TOPAZ)
        register(Blocks.YELLOW_STAINED_GLASS_PANE, TOPAZ_PANE)
        register(Blocks.MAGENTA_STAINED_GLASS, JASPER)
        register(Blocks.MAGENTA_STAINED_GLASS_PANE, JASPER_PANE)
        register(Blocks.BLACK_STAINED_GLASS, ONYX)
        register(Blocks.BLACK_STAINED_GLASS_PANE, ONYX_PANE)
        register(Blocks.BLUE_STAINED_GLASS, AQUAMARINE)
        register(Blocks.BLUE_STAINED_GLASS_PANE, AQUAMARINE_PANE)
        register(Blocks.BROWN_STAINED_GLASS, CITRINE)
        register(Blocks.BROWN_STAINED_GLASS_PANE, CITRINE_PANE)
        register(Blocks.GREEN_STAINED_GLASS, PERIDOT)
        register(Blocks.GREEN_STAINED_GLASS_PANE, PERIDOT_PANE)
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
