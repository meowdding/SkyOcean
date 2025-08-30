package me.owdding.skyocean.datagen.providers

import com.google.common.hash.Hashing
import me.owdding.skyocean.utils.Utils
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import java.util.concurrent.CompletableFuture

data class CopyProvider(val fabricOutputs: FabricDataOutput, val providerName: String, val output: String, val classpath: String) : DataProvider {
    override fun run(output: CachedOutput): CompletableFuture<*> {
        val bytes = Utils.loadFromResourcesAsStream(classpath).use { it.readAllBytes() }

        return CompletableFuture.runAsync { output.writeIfNeeded(fabricOutputs.outputFolder.resolve(this.output), bytes, Hashing.sha256().hashBytes(bytes)) }

    }

    override fun getName() = providerName
}
