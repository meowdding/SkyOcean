package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.config.features.mining.MiningConfig
import codes.cookies.skyocean.events.BlockModelEvent
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.boundingboxes.DwarvenMinesBB
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.*
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object MistBlocks : BlockRetexture() {
    val MIST_SNOW = register(Blocks.SNOW, register("mist_snow", ::SnowLayerBlock))
    val MIST_SNOW_BLOCK = register(Blocks.SNOW_BLOCK, register("mist_snow_block"))
    val MIST_MAIN_GLASS = register(Blocks.WHITE_STAINED_GLASS, register("mist_glass", ::TransparentBlock))
    val MIST_CLAY = register(Blocks.CLAY, register("mist_clay"))
    val MIST_GLASS_PANE = registerMultiple(
        arrayOf(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE),
        register("mist_glass_pane", { StainedGlassPaneBlock(DyeColor.WHITE, it) }),
    )
    val MIST_ICE = register(Blocks.ICE, register("mist_ice", ::IceBlock))
    val MIST_GLASS = register(Blocks.LIGHT_BLUE_STAINED_GLASS, register("mist_glass_secondary", { StainedGlassBlock(DyeColor.PINK, it) }))
    val MIST_CARPET = register(Blocks.WHITE_CARPET, register("mist_carpet", { WoolCarpetBlock(DyeColor.WHITE, it) }))

    init {
        BlockRenderLayerMap.INSTANCE.putBlock(MIST_GLASS_PANE, RenderType.translucent())
        BlockRenderLayerMap.INSTANCE.putBlock(MIST_MAIN_GLASS, RenderType.translucent())
        BlockRenderLayerMap.INSTANCE.putBlock(MIST_GLASS, RenderType.translucent())
        BlockRenderLayerMap.INSTANCE.putBlock(MIST_CARPET, RenderType.cutout())
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.DWARVEN_MINES)
    fun onBlockModelEvent(event: BlockModelEvent) {
        if (!MiningConfig.customMist) return
        if (event.pos !in DwarvenMinesBB.MIST) return
        replaceBlocks(event)
    }
}
