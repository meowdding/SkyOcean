package me.owdding.skyocean.repo.misc

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.RemoteRepoDelegate
import net.minecraft.core.BlockPos

@Module
object GalateaRepoData {
    val data: GalateaData? by RemoteRepoDelegate.load("misc/galatea")
}

@GenerateCodec
data class GalateaData(
    @FieldName("moonglade_beacon_pos") val moongladeBeaconPos: BlockPos,
)
