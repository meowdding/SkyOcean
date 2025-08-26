package me.owdding.skyocean.datagen.dispatcher

import com.google.common.hash.Hashing
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import me.owdding.skyocean.datagen.dispatcher.Utils.zipFile
import me.owdding.skyocean.utils.PACK_FORMAT
import me.owdding.skyocean.utils.PackMetadata
import me.owdding.skyocean.utils.Utils.readAsJson
import java.nio.file.Path
import kotlin.io.path.*

object DataGenPostProcessor {

    @JvmStatic
    fun main(args: Array<String>) {
        val path = Path.of(System.getProperty("skyocean.datagen.output"))
        val intermediates = path.resolve("intermediates")
        intermediates.listDirectoryEntries().filter { it.isDirectory() }.forEach { processPack(it, path) }
    }


    fun <T : Any> JsonElement?.toDataOrThrow(codec: Codec<T>): T {
        return codec.parse(JsonOps.INSTANCE, this).getOrThrow()
    }


    fun processPack(folder: Path, output: Path) = zipFile(output.resolve("${folder.name}.zip")) { root ->
        val versions = folder.listDirectoryEntries("*.zip")

        val info: MutableMap<Path, VersionInfo> = mutableMapOf()

        versions.forEach {
            zipFile(it) { root ->
                val format = root.resolve("pack.mcmeta").readAsJson().toDataOrThrow(PACK_FORMAT)


                info[it] = VersionInfo(
                    format,
                    root.walk().filter { it.isRegularFile() }.map { entry ->
                        val data = entry.readBytes()
                        HashedEntry(entry.toString(), Hashing.sha256().hashBytes(data).asBytes().toHexString(), data)
                    }.toMutableList(),
                )
            }
        }

        print("meow")
    }

    data class VersionInfo(
        val metadata: PackMetadata,
        val entries: MutableList<HashedEntry>,
    )

    data class HashedEntry(val path: String, val hash: String, val data: ByteArray) {
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
