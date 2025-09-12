package me.owdding.skyocean.repo.customization

import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.get
import me.owdding.skyocean.utils.codecs.CodecHelpers
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

@Module
object TrimPatternMap {

    val idMap: Map<ResourceLocation, ResourceLocation> = Utils.loadRepoData("customization/trim_pattern_map", CodecHelpers.map())
    val map = idMap.map { (key, value) ->
        BuiltInRegistries.ITEM.get(key).get().value() to Registries.TRIM_PATTERN.get(value).value()
    }.toMap()

}
