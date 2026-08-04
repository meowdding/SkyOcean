package me.owdding.skyocean.config.patcher

import com.google.gson.JsonArray
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
            put("craft_helper_done_message") {
                val doneMessage = it.getPath("misc.crafthelper.doneMessage")?.asBoolean ?: false
                if (doneMessage) {
                    val craftHelper = it.getAsJsonObject("misc.crafthelper") ?: JsonObject()
                    val doneNotificationConfig = JsonObject().apply {
                        val doneTypes = JsonArray().apply {
                            add("DONE_MESSAGE")
                        }
                        add("doneTypes", doneTypes)
                    }
                    craftHelper.add("doneNotificationConfig", doneNotificationConfig)
                    it.add("misc.crafthelper", craftHelper)
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
