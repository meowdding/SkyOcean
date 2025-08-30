package me.owdding.skyocean.utils.storage

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.Utils.readJson
import me.owdding.skyocean.utils.Utils.writeJson
import org.apache.commons.io.FileUtils
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.profile.ProfileChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import tech.thatgravyboat.skyblockapi.utils.json.JsonObject
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.relativeTo

internal class ProfileStorage<T : Any>(
    private val version: Int = 0,
    private var defaultData: () -> T,
    val fileName: String,
    val codec: (Int) -> Codec<T>,
) {

    @Module
    companion object {
        val requiresSave = mutableSetOf<ProfileStorage<*>>()
        var currentProfile: String? = null

        @Subscription
        fun onProfileSwitch(event: ProfileChangeEvent) {
            currentProfile = event.name
        }

        @Subscription(TickEvent::class)
        @TimePassed("5s")
        fun onTick() {
            val toSave = requiresSave.toTypedArray()
            requiresSave.clear()
            CompletableFuture.runAsync {
                toSave.forEach {
                    it.saveToSystem()
                }
            }
        }

        inline val defaultPath: Path get() = DataStorage.defaultPath
        private fun hasProfile() = currentProfile != null
    }

    private fun isCurrentlyActive() = lastProfile != null && hasProfile() && currentProfile == lastProfile

    private lateinit var data: T
    private lateinit var lastPath: Path
    private var lastProfile: String? = null

    fun get(): T? {
        if (isCurrentlyActive()) {
            return data
        }

        saveToSystem()
        load()

        return if (this::data.isInitialized) data else null
    }

    fun set(new: T) {
        if (isCurrentlyActive()) {
            data = new
            return
        }

        saveToSystem()
        load()
        if (this::data.isInitialized) {
            data = new
        }
    }

    fun save() {
        requiresSave.add(this)
    }

    fun load() {
        if (!hasProfile()) {
            return
        }

        lastProfile = currentProfile
        val lastProfile = lastProfile ?: return
        lastPath = defaultPath.resolve(McPlayer.uuid.toString())
            .resolve(lastProfile)
            .resolve("$fileName.json")

        if (!lastPath.exists()) {
            lastPath.createParentDirectories()
            data = defaultData()
            saveToSystem()
            return
        }

        try {
            val readJson = lastPath.readJson<JsonObject>()
            val version = readJson.get("@skyocean:version").asInt
            val data = readJson.get("@skyocean:data")
            val codec = codec(version)
            this.data = data.toDataOrThrow(codec)
        } catch (e: Exception) {
            SkyOcean.error("Failed to load ${lastPath.relativeTo(defaultPath)}.", e)
            this.data = defaultData()
            saveToSystem()
        }
    }

    private fun saveToSystem() {
        if (!this::data.isInitialized) return
        SkyOcean.debug("Saving $lastPath")
        try {
            val version = this.version
            val codec = this.codec(version)
            val json = JsonObject {
                this["@skyocean:version"] = version
                this["@skyocean:data"] = data.toJson(codec) ?: return SkyOcean.warn("Failed to encode $data to json")
            }
            lastPath.writeJson(json)
            FileUtils.write(lastPath.toFile(), json.toPrettyString(), Charsets.UTF_8)
            SkyOcean.debug("saved $lastPath")
        } catch (e: Exception) {
            SkyOcean.error("Failed to save $data to file")
            e.printStackTrace()
        }
    }

}
