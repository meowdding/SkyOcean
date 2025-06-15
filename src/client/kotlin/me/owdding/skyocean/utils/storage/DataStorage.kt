package me.owdding.skyocean.utils.storage

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.Utils.readJson
import me.owdding.skyocean.utils.Utils.writeJson
import org.apache.commons.io.FileUtils
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import tech.thatgravyboat.skyblockapi.utils.json.JsonObject
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.relativeTo

internal class DataStorage<T : Any>(
    private val version: Int = 0,
    defaultData: () -> T,
    fileName: String,
    codec: (Int) -> Codec<T>,
) {
    constructor(defaultData: () -> T, fileName: String, codec: Codec<T>) : this(0, defaultData, fileName, { codec })

    fun get(): T = data

    fun save() {
        requiresSave.add(this)
    }

    @Module
    companion object {
        val requiresSave = mutableSetOf<DataStorage<*>>()

        @Subscription(TickEvent::class)
        fun onTick() {
            val toSave = requiresSave.toTypedArray()
            requiresSave.clear()
            CompletableFuture.supplyAsync {
                toSave.forEach {
                    it.saveToSystem()
                }
            }
        }
        val defaultPath: Path = McClient.config.resolve("skyocean/data")
    }

    private val path: Path = defaultPath.resolve("$fileName.json")

    private val data: T

    init {
        if (!path.exists()) {
            path.createParentDirectories()
            this.data = defaultData()
        } else {
            var newData: T
            try {
                val readJson = path.readJson<JsonObject>()
                val version = readJson.get("@skyocean:version").asInt
                val data = readJson.get("@skyocean:data")
                val codec = codec(version)
                newData = data.toDataOrThrow(codec)
            } catch (e: Exception) {
                SkyOcean.error("Failed to load ${path.relativeTo(defaultPath)}.", e)
                newData = defaultData()
            }
            this.data = newData
        }
    }

    private val currentCodec = codec(version)

    private fun saveToSystem() {
        SkyOcean.debug("Saving {}", path)
        try {
            val version = this.version
            val json = JsonObject {
                this["@skyocean:version"] = version
                this["@skyocean:data"] = data.toJson(currentCodec) ?: return SkyOcean.warn("Failed to encode {} to json", data)
            }
            path.writeJson(json)
            FileUtils.write(path.toFile(), json.toPrettyString(), Charsets.UTF_8)
            SkyOcean.debug("saved {}", path)
        } catch (e: Exception) {
            SkyOcean.error("Failed to save {} to file", data)
            e.printStackTrace()
        }
    }
}
