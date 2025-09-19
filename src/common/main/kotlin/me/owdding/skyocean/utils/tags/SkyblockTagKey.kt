package me.owdding.skyocean.utils.tags

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils.readAsJson
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.getApiId
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import kotlin.jvm.optionals.getOrNull

@GenerateCodec
data class SkyblockTagFile(
    val values: List<String>,
)

interface SkyblockTagKey<T> {

    val tag: Set<String>
    fun load(): Set<String> = load(toPath(location)).mapTo(HashSet()) { it.lowercase() }

    fun toPath(resourceLocation: ResourceLocation) = "data/${resourceLocation.namespace}/tags/skyblock/${resourceLocation.path}.json"

    companion object {
        private fun getResourcePaths(path: String) = FabricLoader.getInstance().allMods.mapNotNull { mod -> mod.findPath(path).getOrNull() }

        fun load(path: String): List<String> = getResourcePaths(path).flatMap {
            val file = it.readAsJson().toData(SkyOceanCodecs.SkyblockTagFileCodec.codec()) ?: run {
                SkyOcean.error("Failed to load tag file $path")
                return@flatMap emptyList<String>()
            }

            file.values
        }

    }

    val location: ResourceLocation
    operator fun contains(value: T): Boolean
}

interface BaseSkyblockItemTagKey : SkyblockTagKey<ItemStack> {

    override fun toPath(resourceLocation: ResourceLocation) = super.toPath(resourceLocation.withPrefix("item/"))
    override operator fun contains(value: ItemStack) = value.getSkyBlockId()?.cleanId?.lowercase() in this.tag ||
        value.getApiId()?.lowercase() in this.tag ||
        value.getSkyBlockId()?.let { it.cleanId in this.tag || it.skyblockId in this.tag } == true

    operator fun contains(id: String?) = id in this.tag
}
