package me.owdding.skyocean.config.patcher

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.SkyOcean
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.json.getPath

@GenerateCodec
data class AddPatch(
    val path: String,
    val insert: JsonElement,
) : Patch {
    override fun id(): ResourceLocation = ID

    override fun patch(jsonObject: JsonObject) {
        val json = jsonObject.getPath(path.substringBeforeLast("."), true)
        val field = path.substringAfterLast(".")
        when (val element = json?.asJsonObject?.get(field)) {
            is JsonArray -> element.add(insert)
            null -> {}
            else -> error("Not supported yet :3")
        }
    }

    companion object {
        val ID: ResourceLocation = SkyOcean.id("add")
    }
}
