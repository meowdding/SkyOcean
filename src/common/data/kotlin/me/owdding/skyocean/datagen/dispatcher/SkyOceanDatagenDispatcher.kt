package me.owdding.skyocean.datagen.dispatcher

import me.owdding.skyocean.events.DatagenFinishEvent
import net.fabricmc.loader.impl.launch.knot.KnotClient
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.*

object SkyOceanDatagenDispatcher {
    val target = runCatching {
        DatagenTarget.valueOf(System.getProperty("skyocean.datagen.target"))
    }.getOrDefault(DatagenTarget.INCLUDED)

    val packs: MutableList<String> = mutableListOf()

    fun createResourcePack(name: String) {
        packs.add(name)
    }

    lateinit var path: Path

    @JvmStatic
    fun main(args: Array<String>) {
        System.getProperty("skyocean.datagen.dir")?.let {
            System.setProperty("fabric-api.datagen.output-dir", it)
        }
        KnotClient.main(args)
    }

    var hasRegistered = false
    fun register() {
        if (hasRegistered) return
        SkyBlockAPI.eventBus.register(this)
        hasRegistered = true
    }

    @Subscription(DatagenFinishEvent::class)
    fun postProcess() {
        if (target != DatagenTarget.RESOURCE_PACKS) return
        val root = Path.of(System.getProperty("fabric-api.datagen.output-dir"))
        val output = runCatching { Path.of(System.getProperty("skyocean.datagen.output")) }
            .getOrDefault(root)
        val resourcePacks = root.resolve("resourcepacks")
        packs.forEach { pack ->
            val packRoot = resourcePacks.resolve(pack)
            val zipFile = packRoot.walk(PathWalkOption.INCLUDE_DIRECTORIES).zip(packRoot)
            output.resolve("$pack-${McClient.version}.zip").writeBytes(zipFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }
    }

    fun Sequence<Path>.zip(root: Path): ByteArray {

        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use {
            this.forEach { file ->
                val relative = root.relativize(file).toString() + ("/".takeUnless { file.isRegularFile() } ?: "")
                val entry = ZipEntry(relative)
                it.putNextEntry(entry)
                if (!entry.isDirectory) {
                    it.write(file.readBytes())
                }
                it.closeEntry()
            }
        }

        return output.toByteArray()
    }

}

enum class DatagenTarget {
    INCLUDED,
    RESOURCE_PACKS,
    ;
}
