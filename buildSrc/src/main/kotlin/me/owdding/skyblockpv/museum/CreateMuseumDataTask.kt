package me.owdding.skyblockpv.museum

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.owdding.repo.DEFAULT_CACHE_DIRECTORY
import me.owdding.repo.FileCache
import me.owdding.repo.resources.CompactingResourcesExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.getByType
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes
import kotlin.time.Duration.Companion.hours

const val HYPIXEL_ITEM_LIST = "https://api.hypixel.net/v2/resources/skyblock/items"
const val MUSEUM_DATA_CACHE_ENTRY = "museum_data_meow_:3"

@CacheableTask
abstract class CreateMuseumDataTask : DefaultTask() {
    @Internal
    val downloadCache = FileCache(project.gradle.gradleUserHomeDir.toPath().resolve(DEFAULT_CACHE_DIRECTORY), 1.hours)

    @Internal
    val cacheKey = downloadCache.getKey(MUSEUM_DATA_CACHE_ENTRY)

    init {
        val configuration = project.extensions.getByType<CompactingResourcesExtension>()
        val file = project.layout.buildDirectory.file("generated/meowdding/museum_data/${configuration.basePath}/museum_data.json").get().asFile
        fun write(byteArray: ByteArray) {
            val filePath = file.toPath()
            filePath.parent.createDirectories()
            filePath.writeBytes(byteArray, options = arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))
        }

        doFirst {
            if (downloadCache.isCached(cacheKey)) {
                write(downloadCache.read(cacheKey))
                return@doFirst
            }

            val itemList = JsonParser.parseString(downloadCache.getOrDownload(HYPIXEL_ITEM_LIST).toString(Charsets.UTF_8)).asJsonObject["items"].asJsonArray
            val special = JsonArray()
            itemList.forEach {
                val item = it.asJsonObject
                if (item.has("museum_data")) {
                    item.getAsJsonObject("museum_data")?.let { museumData ->
                        val valueOf = MuseumParser.valueOf(museumData.get("type").asString)
                        valueOf.processor.process(item)
                    }
                } else if (item.has("museum")) {
                    special.add(item.get("id"))
                }
            }

            val output = JsonObject()
            MuseumParser.values().forEach { parser ->
                val processor = parser.processor
                val postProcess = processor.postProcess()
                output.add(processor.key, postProcess)
            }
            //output.add("special", special)
            downloadCache.write(cacheKey, output.toString().toByteArray())

            write(GsonBuilder().setPrettyPrinting().create().toJson(output).toByteArray())
        }

        outputs.dir(project.layout.buildDirectory.file("generated/meowdding/museum_data"))
    }


}
