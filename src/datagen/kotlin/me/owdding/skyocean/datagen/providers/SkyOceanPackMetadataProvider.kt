package me.owdding.skyocean.datagen.providers

import me.owdding.skyocean.utils.Utils.jsonObject
import me.owdding.skyocean.utils.Utils.putElement
import me.owdding.skyocean.utils.Utils.putNumber
import me.owdding.skyocean.utils.Utils.putObject
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.McVersionGroup
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJsonOrThrow
import java.util.concurrent.CompletableFuture

class PackMcMetaProvider(val output: FabricDataOutput, val init: PackBuilder.() -> Unit) : DataProvider {
    override fun run(output: CachedOutput): CompletableFuture<*> {
        val builder = PackBuilder().apply(init)

        val element = jsonObject {
            putObject("pack") {
                putElement("description", builder.description.toJsonOrThrow(ComponentSerialization.CODEC))
                putNumber("min_format", builder.minVersion)
                putNumber("max_format", builder.maxVersion)
            }
        }

        return DataProvider.saveStable(output, element, this.output.outputFolder.resolve("pack.mcmeta"))
    }

    override fun getName() = "pack.mcmeta"
}

val versions = mutableMapOf(
    McVersionGroup.MC_1_21_5 to (55 to 55),
    McVersionGroup.MC_1_21_6 to (63 to 64),
    McVersionGroup.MC_1_21_9 to (69 to 69),
)

fun minVersion(): Int = versions[McClient.mcVersionGroup]!!.first
fun maxVersion(): Int = versions[McClient.mcVersionGroup]!!.second

class PackBuilder {
    var description: Component = CommonComponents.EMPTY
    var minVersion: Int = minVersion()
    var maxVersion: Int = maxVersion()
}
