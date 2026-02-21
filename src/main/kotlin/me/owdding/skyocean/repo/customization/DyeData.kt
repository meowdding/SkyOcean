package me.owdding.skyocean.repo.customization

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow

@LateInitModule
object DyeData {
    val animatedDyes: MutableMap<String, List<Int>> = mutableMapOf()
    val staticDyes: MutableMap<String, Int> = mutableMapOf()

    init {
        runCatching {
            val element = Utils.loadFromRemoteRepo<JsonElement>("skyocean/dyes")!!.asJsonObject

            val lower: Codec<String> = Codec.STRING.xmap({ it.lowercase() }, { it })
            val hexCodec: Codec<Int> = Codec.STRING.xmap({ it.removePrefix("#").toInt(16) }, { "#${it.toString(16)}" })

            animatedDyes.putAll(element.get("animated").toDataOrThrow(CodecUtils.map(lower, hexCodec.listOf())))
            staticDyes.putAll(element.get("static").toDataOrThrow(CodecUtils.map(lower, hexCodec)))
        }.onFailure {
            throw RuntimeException("Failed to load dye data!", it)
        }
    }

    fun getAnimated(id: String, offset: Int): Int {
        val colors = animatedDyes[id]!!
        return colors[(TickEvent.ticks / 2 + offset) % colors.size]
    }
}
