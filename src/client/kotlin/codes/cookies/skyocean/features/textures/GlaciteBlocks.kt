package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.config.features.mining.MiningConfig
import codes.cookies.skyocean.events.BlockModelEvent
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.boundingboxes.DwarvenMinesBB
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object GlaciteBlocks : BlockRetexture() {
    //val GLACITE_SNOW = register(Blocks.SNOW, register("glacite_snow", ::SnowLayerBlock))
    //val GLACITE_SNOW_BLOCK = register(Blocks.SNOW_BLOCK, register("glacite_snow_block"))
    //val GLACITE = register(Blocks.PACKED_ICE, register("glacite"))
    //val GLACITE_HARD_STONE = register(Blocks.INFESTED_STONE, register("glacite_hard_stone"))
    //val GLACITE_HARD_STONE_WOOL = register(Blocks.LIGHT_GRAY_WOOL, register("glacite_hard_stone_wool"))

    @Subscription
    @OnlyIn(SkyBlockIsland.DWARVEN_MINES)
    fun onBlockModelEvent(event: BlockModelEvent) {
        if (!MiningConfig.customMiningTextures) return
        if (event.pos !in DwarvenMinesBB.GLACITE_TUNNELS) return
        replaceBlocks(event)
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.MINESHAFT)
    fun onMineshaftBlockModelEvent(event: BlockModelEvent) {
        if (!MiningConfig.customMiningTextures) return
        if (event.block == Blocks.STONE) {
            //event.state = GLACITE_HARD_STONE.defaultBlockState()
            return
        }
        replaceBlocks(event)
    }
}
