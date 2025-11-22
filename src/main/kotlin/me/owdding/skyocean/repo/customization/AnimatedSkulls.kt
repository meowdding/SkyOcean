package me.owdding.skyocean.repo.customization

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.codecs.CodecHelpers
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import kotlin.math.max

@Module
object AnimatedSkulls {

    val ids: MutableList<String> = mutableListOf()
    val skins: MutableMap<SkyBlockId, AnimatedSkullData> = mutableMapOf()

    @IncludedCodec(named = "min_1")
    val minOne: Codec<Int> = Codec.INT.xmap({ max(it, 1) }, { it })

    @IncludedCodec(named = "texture_codec")
    val textureCodec: Codec<List<String>> = Codec.STRING.xmap({ it.substringAfter(':') }, { it }).listOf()

    init {
        runCatching {
            val skulls = Utils.loadFromRepo<JsonElement>("skulls")!!.asJsonObject
            ids.addAll(skulls.get("help").toDataOrThrow(CodecUtils.map(CodecHelpers.STRING_LOWER, CodecUtils.JSON_ELEMENT_CODEC)).keys)
            skins.putAll(
                skulls.get("skins").toDataOrThrow(
                    CodecUtils.map(
                        CodecHelpers.STRING_LOWER,
                        SkyOceanCodecs.AnimatedSkullDataCodec.codec(),
                    ),
                ).mapKeys { (key) -> SkyBlockId.item(key) },
            )
        }.onFailure {
            throw RuntimeException("Failed to load animated skulls!", it)
        }
    }

    @GenerateCodec
    data class AnimatedSkullData(
        @NamedCodec("min_1") val ticks: Int = 2,
        @NamedCodec("texture_codec") val textures: List<String>,
    ) {
        fun getTexture(): String {
            return textures[(TickEvent.ticks / ticks) % textures.size]
        }
    }
}
