package me.owdding.skyocean.features.text

import com.google.gson.JsonObject
import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils.unsafeCast
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJsonOrThrow
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object TextReplacementUtils {

    const val DATA_PREFIX = "<skyocean:textreplacement>"

    interface TextReplacementData<Type : Any> {
        val category: TextReplacementCategory
        val textReplacements: List<TextReplacement>
        val codec: MapCodec<Type>
        val version: Int
        fun self(): Type = this.unsafeCast()

        fun encode(): String = self().toJsonOrThrow(codec.codec()).toString()
    }

    @GenerateCodec
    @Suppress("ClassName")
    data class v1TextReplacementData(
        override val category: TextReplacementCategory,
        override val textReplacements: List<TextReplacement>,
    ) : TextReplacementData<v1TextReplacementData> {
        override val version: Int = 1
        override val codec: MapCodec<v1TextReplacementData> = SkyOceanCodecs.v1TextReplacementDataCodec
    }

    private fun createLatest(
        category: TextReplacementCategory,
        textReplacements: List<TextReplacement>,
    ) = v1TextReplacementData(category, textReplacements)

    fun writeData(category: TextReplacementCategory): Result<String> = runCatching {
        val newUuid = UUID.randomUUID()
        val newCategory = category.copy(identifier = newUuid)
        val textReplacements = category.getReplacementsInCategory().map {
            it.copy(category = newUuid)
        }

        val latest: TextReplacementData<*> = createLatest(newCategory, textReplacements)
        val byteArray = ByteArrayOutputStream()
        byteArray.use {
            GZIPOutputStream(it).use { gzip ->
                gzip.write(latest.version)
                gzip.write(latest.encode().toByteArray())
            }
        }

        DATA_PREFIX + Base64.getEncoder().encodeToString(byteArray.toByteArray())
    }

    fun readData(data: String): Result<TextReplacementData<*>> = runCatching {
        val data = data.removePrefix(DATA_PREFIX)
        val (version, rawData) = GZIPInputStream(ByteArrayInputStream(Base64.getDecoder().decode(data))).use {
            it.read() to it.readAllBytes().decodeToString().readJson<JsonObject>()
        }

        val codec: MapCodec<out TextReplacementData<*>> = when (version) {
            1 -> SkyOceanCodecs.v1TextReplacementDataCodec
            else -> throw UnsupportedOperationException("Unknown version $version!")
        }

        rawData.toDataOrThrow(codec.codec())
    }

}
