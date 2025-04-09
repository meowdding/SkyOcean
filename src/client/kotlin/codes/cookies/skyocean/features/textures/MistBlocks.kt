package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.modules.Module

@Module
object MistBlocks : BlockRetexture() {
   /*
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
*/
    init {
        //BlockRenderLayerMap.INSTANCE.putBlock(MIST_GLASS_PANE, RenderType.translucent())
        //BlockRenderLayerMap.INSTANCE.putBlock(MIST_MAIN_GLASS, RenderType.translucent())
        //BlockRenderLayerMap.INSTANCE.putBlock(MIST_GLASS, RenderType.translucent())
        //BlockRenderLayerMap.INSTANCE.putBlock(MIST_CARPET, RenderType.cutout())
    }
}
