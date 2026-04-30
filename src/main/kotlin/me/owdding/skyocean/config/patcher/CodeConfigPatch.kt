package me.owdding.skyocean.config.patcher

import com.google.gson.JsonObject
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.SkyOcean
import net.minecraft.resources.Identifier
import tech.thatgravyboat.skyblockapi.utils.json.getPath

@GenerateCodec
data class CodeConfigPatch(val name: String) : Patch {
    companion object {
        val patches = buildMap<String, (JsonObject) -> Unit> {
            put("fishing_hook_scale") {
                val scale = it.getPath("fishing.hookTextScale")?.asFloat ?: 1f
                if (scale != 1f) {
                    val fishing = it.getAsJsonObject("fishing") ?: JsonObject()
                    fishing.addProperty("hookTextScaleToggle", true)
                    it.add("fishing", fishing)
                }
            }
        }

        val ID: Identifier = SkyOcean.id("code")
    }

    override fun id(): Identifier = ID

    override fun patch(jsonObject: JsonObject) {
        patches[name]?.invoke(jsonObject)
    }
}
