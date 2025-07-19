package me.owdding.skyocean.config.patcher

import com.google.gson.JsonObject
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.skyocean.SkyOcean
import net.minecraft.resources.ResourceLocation

data class CompoundPatch(val patches: List<Patch>) : Patch {
    companion object {
        val ID: ResourceLocation = SkyOcean.id("compound")
        val CODEC: MapCodec<CompoundPatch> = RecordCodecBuilder.mapCodec {
            it.group(
                ConfigPatches.CODEC.listOf().fieldOf("patches").forGetter(CompoundPatch::patches),
            ).apply(it, ::CompoundPatch)
        }
    }

    override fun id() = ID
    override fun patch(jsonObject: JsonObject) {
        patches.forEach {
            it.patch(jsonObject)
        }
    }
}
