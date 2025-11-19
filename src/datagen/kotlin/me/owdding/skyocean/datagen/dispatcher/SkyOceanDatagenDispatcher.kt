package me.owdding.skyocean.datagen.dispatcher

import me.owdding.skyocean.datagen.dispatcher.Utils.zip
import me.owdding.skyocean.events.DatagenFinishEvent
import net.fabricmc.loader.impl.launch.knot.KnotClient
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.PathWalkOption
import kotlin.io.path.createParentDirectories
import kotlin.io.path.walk
import kotlin.io.path.writeBytes

object SkyOceanDatagenDispatcher {
    val target = runCatching {
        DatagenTarget.valueOf(System.getProperty("skyocean.datagen.target"))
    }.getOrDefault(DatagenTarget.INCLUDED)

    val packs: MutableList<String> = mutableListOf()
    lateinit var path: Path
    var hasRegistered = false

    fun createResourcePack(name: String) {
        packs.add(name)
    }


    @JvmStatic
    fun main(args: Array<String>) {
        System.getProperty("skyocean.datagen.dir")?.let {
            System.setProperty("fabric-api.datagen.output-dir", it)
        }
        KnotClient.main(args)
    }

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
            output.resolve("intermediates/$pack/${McClient.version}.zip").apply { createParentDirectories() }
                .writeBytes(zipFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }
    }

}

enum class DatagenTarget {
    INCLUDED,
    RESOURCE_PACKS,
}
