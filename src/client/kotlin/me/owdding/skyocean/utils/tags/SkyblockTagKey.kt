package me.owdding.skyocean.utils.tags

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils.readAsJson
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.getApiId
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import java.nio.file.Path
import java.util.function.Consumer

@GenerateCodec
data class SkyblockTagFile(
    val values: List<String>,
    val replace: Boolean = false,
)

interface SkyblockTagKey<T> {

    val tag: List<String>
    fun load() = load(toPath(location)).map { it.lowercase() }

    fun toPath(resourceLocation: ResourceLocation) = "data/${resourceLocation.namespace}/tags/skyblock/${resourceLocation.path}.json"

    companion object {
        private fun getResourcePaths(path: String): HashSet<Path> {
            val out = HashSet<Path>()

            for (mod in FabricLoader.getInstance().allMods) {
                mod.findPath(path).ifPresent(Consumer { e: Path? -> out.add(e!!) })
            }

            return out
        }

        fun load(path: String): List<String> {
            val list = mutableListOf<String>()

            getResourcePaths(path).forEach { it ->
                val file = it.readAsJson().toData(SkyOceanCodecs.SkyblockTagFileCodec.codec()) ?: run {
                    SkyOcean.error("Failed to load tag file $path")
                    return@forEach
                }
                if (file.replace) list.clear()
                list.addAll(file.values)
            }

            return list
        }

    }

    val location: ResourceLocation
    operator fun contains(value: T): Boolean
}

interface BaseSkyblockItemTagKey : SkyblockTagKey<ItemStack> {

    override fun toPath(resourceLocation: ResourceLocation) = super.toPath(resourceLocation.withPrefix("item/"))
    override operator fun contains(value: ItemStack) = value.getSkyBlockId()?.lowercase() in this.tag || value.getApiId()?.lowercase() in this.tag

    operator fun contains(id: String) = id in this.tag
}
