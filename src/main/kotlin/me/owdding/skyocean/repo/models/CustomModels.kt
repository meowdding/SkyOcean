package me.owdding.skyocean.repo.models

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils
import net.minecraft.util.ExtraCodecs

@Module
object CustomModels {
    val ID_MAPPER = ExtraCodecs.LateBoundIdMapper<String, MapCodec<out SkyOceanModel>>()

    @IncludedCodec
    val CODEC: Codec<SkyOceanModel> = ID_MAPPER.codec(Codec.STRING).dispatch(SkyOceanModel::codec) { it }

    init {
        ID_MAPPER.put("player_head", SkyOceanCodecs.PlayerHeadModelCodec)
        ID_MAPPER.put("block", SkyOceanCodecs.SingleBlockSupplierCodec)
        ID_MAPPER.put("alternating", SkyOceanCodecs.AlternatingModelCodec)
        ID_MAPPER.put("composite", SkyOceanCodecs.CompositeModelCodec)
    }

    val models: Map<String, SkyOceanModel> = Utils.loadRepoData("custom_models", Codec.unboundedMap(Codec.STRING, CODEC))
}

