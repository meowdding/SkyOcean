package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.config.features.mining.MiningConfig
import codes.cookies.skyocean.events.BlockModelEvent
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.boundingboxes.DwarvenMinesBB
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.Block
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object GemstoneBlocks : BlockRetexture() {
    /*val RUBY = register(Blocks.RED_STAINED_GLASS, register("ruby", ::TransparentBlock))
    val RUBY_PANE = register(Blocks.RED_STAINED_GLASS_PANE, register("ruby_pane", ::IronBarsBlock))

    val AMBER = register(Blocks.ORANGE_STAINED_GLASS, register("amber", ::TransparentBlock))
    val AMBER_PANE = register(Blocks.ORANGE_STAINED_GLASS_PANE, register("amber_pane", ::IronBarsBlock))

    val SAPPHIRE = register(Blocks.LIGHT_BLUE_STAINED_GLASS, register("sapphire", ::TransparentBlock))
    val SAPPHIRE_PANE = register(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, register("sapphire_pane", ::IronBarsBlock))

    val JADE = register(Blocks.LIME_STAINED_GLASS, register("jade", ::TransparentBlock))
    val JADE_PANE = register(Blocks.LIME_STAINED_GLASS_PANE, register("jade_pane", ::IronBarsBlock))

    val AMETHYST = register(Blocks.PURPLE_STAINED_GLASS, register("amethyst", ::TransparentBlock))
    val AMETHYST_PANE = register(Blocks.PURPLE_STAINED_GLASS_PANE, register("amethyst_pane", ::IronBarsBlock))

    val OPAL = register(Blocks.WHITE_STAINED_GLASS, register("opal", ::TransparentBlock))
    val OPAL_PANE = register(Blocks.WHITE_STAINED_GLASS_PANE, register("opal_pane", ::IronBarsBlock))

    val TOPAZ = register(Blocks.YELLOW_STAINED_GLASS, register("topaz", ::TransparentBlock))
    val TOPAZ_PANE = register(Blocks.YELLOW_STAINED_GLASS_PANE, register("topaz_pane", ::IronBarsBlock))

    val JASPER = register(Blocks.MAGENTA_STAINED_GLASS, register("jasper", ::TransparentBlock))
    val JASPER_PANE = register(Blocks.MAGENTA_STAINED_GLASS_PANE, register("jasper_pane", ::IronBarsBlock))

    val ONYX = register(Blocks.BLACK_STAINED_GLASS, register("onyx", ::TransparentBlock))
    val ONYX_PANE = register(Blocks.BLACK_STAINED_GLASS_PANE, register("onyx_pane", ::IronBarsBlock))

    val AQUAMARINE = register(Blocks.BLUE_STAINED_GLASS, register("aquamarine", ::TransparentBlock))
    val AQUAMARINE_PANE = register(Blocks.BLUE_STAINED_GLASS_PANE, register("aquamarine_pane", ::IronBarsBlock))

    val CITRINE = register(Blocks.BROWN_STAINED_GLASS, register("citrine", ::TransparentBlock))
    val CITRINE_PANE = register(Blocks.BROWN_STAINED_GLASS_PANE, register("citrine_pane", ::IronBarsBlock))

    val PERIDOT = register(Blocks.GREEN_STAINED_GLASS, register("peridot", ::TransparentBlock))
    val PERIDOT_PANE = register(Blocks.GREEN_STAINED_GLASS_PANE, register("peridot_pane", ::IronBarsBlock))
*/
    override fun register(defaultBlock: Block, newBlock: Block): Block {
        BlockRenderLayerMap.INSTANCE.putBlock(newBlock, RenderType.translucent())
        return super.register(defaultBlock, newBlock)
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.DWARVEN_MINES)
    fun onDwarvenMinesBlockModelEvent(event: BlockModelEvent) {
        if (!MiningConfig.customGemstoneTextures) return
        if (!DwarvenMinesBB.GEMSTONE_LOCATIONS.isInside(event.pos)) return
        replaceBlocks(event)
    }

}
