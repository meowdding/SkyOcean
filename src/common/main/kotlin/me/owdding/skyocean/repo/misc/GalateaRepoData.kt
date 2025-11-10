package me.owdding.skyocean.repo.misc

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.Utils
import net.minecraft.core.BlockPos

@Module
object GalateaRepoData {
    val data: GalateaData? = Utils.loadFromRemoteRepo<GalateaData>("misc/galatea")
}

@GenerateCodec
data class GalateaData(
    @NamedCodec("moonglade_beacon_pos") val moongladeBeaconPos: BlockPos,
)
