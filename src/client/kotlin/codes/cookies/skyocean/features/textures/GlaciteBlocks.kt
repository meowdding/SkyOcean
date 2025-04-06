package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.config.features.mining.MiningConfig
import codes.cookies.skyocean.events.BlockModelEvent
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.aabb.DwarvenMinesBB
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SnowLayerBlock
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object GlaciteBlocks {
    val GLACITE_SNOW = register("glacite_snow", ::SnowLayerBlock)
    val GLACITE_SNOW_BLOCK = register("glacite_snow_block")
    val GLACITE = register("glacite")
    val GLACITE_HARD_STONE = register("glacite_hard_stone")
    val GLACITE_HARD_STONE_WOOL = register("glacite_hard_stone_wool")

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
        if (event.state.`is`(Blocks.STONE)) {
            event.state = GLACITE_HARD_STONE.defaultBlockState()
            return
        }
        replaceBlocks(event)
    }

    private fun replaceBlocks(event: BlockModelEvent) {
        event.state = when {
            event.state.`is`(Blocks.SNOW) -> GLACITE_SNOW.withPropertiesOf(event.state)
            event.state.`is`(Blocks.SNOW_BLOCK) -> GLACITE_SNOW_BLOCK.defaultBlockState()
            event.state.`is`(Blocks.PACKED_ICE) -> GLACITE.defaultBlockState()
            event.state.`is`(Blocks.INFESTED_STONE) -> GLACITE_HARD_STONE.defaultBlockState()
            event.state.`is`(Blocks.LIGHT_GRAY_WOOL) -> GLACITE_HARD_STONE_WOOL.defaultBlockState()
            else -> event.state
        }
    }
}
