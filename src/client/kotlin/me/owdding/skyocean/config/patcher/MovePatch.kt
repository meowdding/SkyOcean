package me.owdding.skyocean.config.patcher

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.skyocean.SkyOcean
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class MovePatch(val from: String, val to: String) : Patch {
    companion object {
        val ID: ResourceLocation = SkyOcean.id("move")
        val CODEC: MapCodec<MovePatch> = RecordCodecBuilder.mapCodec {
            it.group(
                Codec.STRING.fieldOf("from").forGetter(MovePatch::from),
                Codec.STRING.fieldOf("to").forGetter(MovePatch::to),
            ).apply(it, ::MovePatch)
        }
    }

    override fun id() = ID
    override fun patch(jsonObject: JsonObject) {
        val path = jsonObject.getPath(from)
        val parent = to.substringBeforeLast(".", "")
        val name = to.substringAfterLast(".")
        jsonObject.getPath(parent, true)?.asJsonObject?.add(name, path)
    }
}
