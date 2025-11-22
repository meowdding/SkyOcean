package me.owdding.skyocean.utils.storage

import com.mojang.serialization.Codec
import me.owdding.skyocean.SkyOcean
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.nio.file.Path
import kotlin.io.path.*

class FolderStorage<T : Any>(
    val folder: String,
    val codec: Codec<T>,
) {
    private val storages = mutableMapOf<String, DataStorage<T>>()
    private val defaultPath: Path = McClient.config.resolve("skyocean/data/$folder")

    init {
        load()
    }

    fun load() {
        this.storages.putAll(
            files().mapNotNull {
                val id = it.nameWithoutExtension
                try {
                    id to DataStorage(
                        version = 0,
                        defaultData = { throw IllegalStateException("No default data for folder storage!") },
                        fileName = "$folder/$id",
                        codec = { codec },
                    )
                } catch (e: Exception) {
                    SkyOcean.error("Failed to load storage file: ${it.relativeTo(McClient.config)}", e)
                    null
                }
            },
        )
    }

    fun add(value: T) = set(value.hashCode().toString(), value)

    fun set(id: String, value: T) {
        storages.getOrPut(id) {
            DataStorage(
                version = 0,
                defaultData = { value },
                fileName = "$folder/$id",
                codec = { codec },
            )
        }.save()
    }

    fun get(id: String): T? = storages[id]?.get()

    fun remove(id: String) {
        storages.remove(id)?.delete()
    }

    private fun files() =
        defaultPath.apply { createDirectories() }.listDirectoryEntries("*.json").toList().filter { it.isRegularFile() && it.extension == "json" }

    internal fun getStorages() = storages
    fun getAll(): Map<String, T> = storages.mapValues { it.value.get() }

    fun refresh() {
        storages.clear()
        load()
    }
}
