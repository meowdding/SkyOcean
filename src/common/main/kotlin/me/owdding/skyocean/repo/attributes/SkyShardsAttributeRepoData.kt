package me.owdding.skyocean.repo.attributes

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils

@Module
object SkyShardsAttributeRepoData {
    val data: MutableMap<SkyOceanItemId, SkyShardsAttributeData> = Utils.loadRepoData(
        "skyshards_data",
        CodecUtils.map(
            SkyOceanItemId.UNKNOWN_CODEC,
            SkyOceanCodecs.SkyShardsAttributeDataCodec.codec(),
        ).fieldOf("shards").codec(),
    )

}

@GenerateCodec
data class SkyShardsAttributeData(
    @FieldName("fuse_amount") val fuseAmount: Int,
)
