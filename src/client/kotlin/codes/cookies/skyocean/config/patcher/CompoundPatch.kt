package codes.cookies.skyocean.config.patcher

import codes.cookies.skyocean.SkyOcean
import com.google.gson.JsonObject
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
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
