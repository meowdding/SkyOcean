package codes.cookies.skyocean.features.mining

import codes.cookies.skyocean.config.features.mining.MiningConfig
import codes.cookies.skyocean.events.BlockModelEvent
import me.owdding.ktmodules.Module
import codes.cookies.skyocean.utils.tags.BlockTagKey
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object RedCarpets {

    @Subscription
    @OnlyIn(SkyBlockIsland.DWARVEN_MINES)
    fun onBlockModel(event: BlockModelEvent) {
        if (!MiningConfig.recolorCarpets) return
        if (event.block !in BlockTagKey.DWARVEN_MINES_CARPETS) return
        event.block = Blocks.RED_CARPET
    }

}
