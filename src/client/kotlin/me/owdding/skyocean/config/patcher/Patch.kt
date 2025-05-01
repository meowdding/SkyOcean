package me.owdding.skyocean.config.patcher

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import java.util.function.UnaryOperator

interface Patch : UnaryOperator<JsonObject> {

    override fun apply(t: JsonObject): JsonObject {
        patch(t)
        return t
    }

    fun id(): ResourceLocation
    fun patch(jsonObject: JsonObject)

}
