package me.owdding.skyocean.datagen.providers

import com.google.common.hash.HashCode
import com.google.common.hash.HashingOutputStream
import net.minecraft.data.CachedOutput
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import org.apache.commons.io.output.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture

private data class PngOutput(
    val path: ResourceLocation,
    val bytes: ByteArray,
    val hash: HashCode,
)

data class PngHolder(val provider: PackOutput.PathProvider) {
    private val toSave: MutableList<PngOutput> = mutableListOf()

    fun save(output: CachedOutput): CompletableFuture<*> = CompletableFuture.allOf(
        *toSave.map { (path, data, hash) ->
            println(path)
            CompletableFuture.runAsync { output.writeIfNeeded(provider.file(path, "png"), data, hash) }
        }.toTypedArray(),
    )

    fun submit(id: ResourceLocation, outputStream: ByteArrayOutputStream, hashingOutputStream: HashingOutputStream) {
        submit(id, outputStream.toByteArray(), hashingOutputStream.hash())
    }

    fun submit(id: ResourceLocation, data: ByteArray, hash: HashCode) {
        toSave.add(PngOutput(id, data, hash))
    }
}
