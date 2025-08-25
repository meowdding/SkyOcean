package me.owdding.skyocean.datagen.providers

import me.owdding.skyocean.utils.Utils.jsonObject
import me.owdding.skyocean.utils.Utils.putNumber
import me.owdding.skyocean.utils.Utils.putObject
import me.owdding.skyocean.utils.Utils.putString
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.SharedConstants
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import java.util.concurrent.CompletableFuture

class PackMcMetaProvider(val output: FabricDataOutput, val init: PackBuilder.() -> Unit) : DataProvider {
    override fun run(output: CachedOutput): CompletableFuture<*> {
        val builder = PackBuilder().apply(init)

        val element = jsonObject {
            putObject("pack") {
                putString("description", builder.description)
                putNumber("pack_format", builder.version)
            }
        }

        return DataProvider.saveStable(output, element, this.output.outputFolder.resolve("pack.mcmeta"))
    }

    override fun getName() = "pack.mcmeta"
}

class PackBuilder {
    var description: String = ""
    var version: Int = SharedConstants.RESOURCE_PACK_FORMAT
}
