package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.config.features.mining.MiningConfig
import codes.cookies.skyocean.events.BlockModelEvent
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.aabb.DwarvenMinesBB
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.*
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object MistBlocks {
    val MIST_SNOW = register("mist_snow", ::SnowLayerBlock)
    val MIST_SNOW_BLOCK = register("mist_snow_block")
    val MIST_MAIN_GLASS = register("mist_glass", { TransparentBlock(it) })
    val MIST_CLAY = register("mist_clay")
    val MIST_GLASS_PANE = register("mist_glass_pane", { StainedGlassPaneBlock(DyeColor.WHITE, it) })
    val MIST_ICE = register("mist_ice", ::IceBlock)

    init {
        BlockRenderLayerMap.INSTANCE.putBlock(MIST_GLASS_PANE, RenderType.translucent())
        BlockRenderLayerMap.INSTANCE.putBlock(MIST_MAIN_GLASS, RenderType.translucent())
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.DWARVEN_MINES)
    fun onBlockModelEvent(event: BlockModelEvent) {
        if (!MiningConfig.customMist) return
        if (event.pos !in DwarvenMinesBB.MIST) return
        event.state = when {
            event.state.`is`(Blocks.SNOW) -> MIST_SNOW.withPropertiesOf(event.state)
            event.state.`is`(Blocks.SNOW_BLOCK) -> MIST_SNOW_BLOCK.defaultBlockState()
            event.state.`is`(Blocks.ICE) -> MIST_ICE.defaultBlockState()
            event.state.`is`(Blocks.CLAY) -> MIST_CLAY.defaultBlockState()
            event.state.`is`(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE) || event.state.`is`(Blocks.BLUE_STAINED_GLASS_PANE) -> MIST_GLASS_PANE.withPropertiesOf(event.state)
            event.state.`is`(Blocks.WHITE_STAINED_GLASS) -> MIST_MAIN_GLASS.defaultBlockState()
            else -> event.state
        }
    }
}
