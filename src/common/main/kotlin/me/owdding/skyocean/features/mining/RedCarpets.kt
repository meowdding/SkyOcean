package me.owdding.skyocean.features.mining

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningRetexture
import me.owdding.skyocean.events.BlockModelEvent
import me.owdding.skyocean.utils.tags.BlockTagKey
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object RedCarpets {

    @Subscription
    @OnlyIn(SkyBlockIsland.DWARVEN_MINES)
    fun onBlockModel(event: BlockModelEvent) {
        if (!MiningRetexture.recolorCarpets) return
        if (event.block !in BlockTagKey.DWARVEN_MINES_CARPETS) return
        event.block = Blocks.RED_CARPET
    }

}
