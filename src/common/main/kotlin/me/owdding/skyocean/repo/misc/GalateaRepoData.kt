package me.owdding.skyocean.repo.misc

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.skyocean.utils.Utils
import net.minecraft.core.BlockPos
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription

@Module
object GalateaRepoData {
    var data: GalateaData? = null
        private set

    @Subscription(FinishRepoLoadingEvent::class)
    fun onRepoLoad() {
        data = Utils.loadFromRemoteRepo<GalateaData>("misc/galatea")
    }
}

@GenerateCodec
data class GalateaData(
    @FieldName("moonglade_beacon_pos") val moongladeBeaconPos: BlockPos,
)
