//? > 1.21.8 {
package me.owdding.skyocean.features.hotkeys

import com.google.gson.JsonObject
import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.features.hotkeys.system.Hotkey
import me.owdding.skyocean.features.hotkeys.system.HotkeyCategory
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils.unsafeCast
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJsonOrThrow
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.text.decodeToString

object HotkeyUtils {
    const val DATA_PREFIX = "<skyocean:keybinds>"

    interface HotkeyData<Type : Any> {
        val category: HotkeyCategory
        val hotkeys: List<Hotkey>
        val codec: MapCodec<Type>
        val version: Int
        fun self(): Type = this.unsafeCast()

        fun encode(): String = self().toJsonOrThrow(codec.codec()).toString()
    }

    @GenerateCodec
    @Suppress("ClassName")
    data class v1HotkeyData(
        override val category: HotkeyCategory,
        override val hotkeys: List<Hotkey>,
    ) : HotkeyData<v1HotkeyData> {
        override val version: Int = 1
        override val codec: MapCodec<v1HotkeyData> = SkyOceanCodecs.v1HotkeyDataCodec
    }

    private fun createLatest(
        category: HotkeyCategory,
        hotkeys: List<Hotkey>,
    ) = v1HotkeyData(category, hotkeys)

    fun writeData(category: HotkeyCategory): Result<String> = runCatching {
        val newUuid = UUID.randomUUID()
        val newCategory = category.copy(identifier = newUuid)
        val hotkeys = category.getHotkeysInCategory().map {
            it.copy(group = newUuid)
        }

        val latest: HotkeyData<*> = createLatest(newCategory, hotkeys)
        val byteArray = ByteArrayOutputStream()
        byteArray.use {
            GZIPOutputStream(it).use { gzip ->
                gzip.write(latest.version)
                gzip.write(latest.encode().toByteArray())
            }
        }

        DATA_PREFIX + Base64.getEncoder().encodeToString(byteArray.toByteArray())
    }

    fun readData(data: String): Result<HotkeyData<*>> = runCatching {
        val data = data.removePrefix(DATA_PREFIX)
        val (version, rawData) = GZIPInputStream(ByteArrayInputStream(Base64.getDecoder().decode(data))).use {
            it.read() to it.readAllBytes().decodeToString().readJson<JsonObject>()
        }

        val codec: MapCodec<out HotkeyData<*>> = when (version) {
            1 -> SkyOceanCodecs.v1HotkeyDataCodec
            else -> throw UnsupportedOperationException("Unknown version $version!")
        }

        rawData.toDataOrThrow(codec.codec())
    }

}
//? }
