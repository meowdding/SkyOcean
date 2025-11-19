package me.owdding.skyocean.datagen.dispatcher

import com.google.common.hash.Hashing
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import me.owdding.skyocean.datagen.dispatcher.Utils.zipFile
import me.owdding.skyocean.utils.PackMetadata
import me.owdding.skyocean.utils.PackOverlay
import me.owdding.skyocean.utils.Utils.readAsJson
import me.owdding.skyocean.utils.Utils.writeJson
import me.owdding.skyocean.utils.codecs.PACK_FORMAT
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

object DataGenPostProcessor {

    @JvmStatic
    fun main(args: Array<String>) {
        val path = Path.of(System.getProperty("skyocean.datagen.output"))
        val intermediates = path.resolve("intermediates")
        intermediates.listDirectoryEntries().filter { it.isDirectory() }.forEach { processPack(it, path) }
    }


    fun <T : Any> JsonElement?.toDataOrThrow(codec: Codec<T>): T = codec.parse(JsonOps.INSTANCE, this).getOrThrow()

    fun <T : Any> T.toJsonOrThrow(codec: Codec<T>): JsonElement = codec.encodeStart(JsonOps.INSTANCE, this).getOrThrow()

    fun processPack(folder: Path, output: Path) = zipFile(output.resolve("${folder.name}.zip")) { root ->
        val versions = folder.listDirectoryEntries("*.zip")

        val info: MutableMap<Path, VersionInfo> = mutableMapOf()

        versions.forEach {
            zipFile(it) { root ->
                val format = root.resolve("pack.mcmeta").readAsJson().toDataOrThrow(PACK_FORMAT)


                info[it] = VersionInfo(
                    format,
                    root.walk().filter { entry -> entry.isRegularFile() }.map { entry ->
                        val data = entry.readBytes()
                        HashedEntry(entry.toString(), Hashing.sha256().hashBytes(data).asBytes().toHexString(), data, it.nameWithoutExtension)
                    }.toMutableList(),
                )
            }
        }

        val metatadata = info.values.fold(info.values.first().metadata) { first, second -> first.merge(second.metadata) }

        val versionOverlays = mutableListOf<String>()

        info.values.map { it.entries }.flatten().groupBy { it.path }.forEach { (path, entries) ->
            if (entries.size == info.size && entries.distinctBy { it.hash }.count() == 1) {
                val newPath = root.resolve(path)
                newPath.createParentDirectories().writeBytes(entries.first().data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                return@forEach
            }

            entries.forEach { (path, _, data, version) ->
                if (path == "/pack.mcmeta") {
                    return@forEach
                }
                versionOverlays.add(version)
                root.resolve("$version/$path").createParentDirectories().writeBytes(data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            }
        }

        info.forEach { (path, info) ->
            if (path.nameWithoutExtension in versionOverlays) {
                metatadata.add(PackOverlay(path.nameWithoutExtension, info.metadata.pack.formats))
            }
        }

        root.resolve("pack.mcmeta").writeJson(metatadata.toJsonOrThrow(PACK_FORMAT))
    }

    data class VersionInfo(
        val metadata: PackMetadata,
        val entries: MutableList<HashedEntry>,
    )

    data class HashedEntry(val path: String, val hash: String, val data: ByteArray, val version: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HashedEntry) return false

            if (path != other.path) return false
            if (hash != other.hash) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + hash.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

}
