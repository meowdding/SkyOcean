package me.owdding.skyocean.config.patcher

import com.google.gson.JsonObject
import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.resources.Identifier
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import kotlin.text.substringAfterLast
import kotlin.text.substringBeforeLast

@GenerateCodec
data class RemovePatch(val path: String) : Patch {

    companion object {
        val ID: Identifier = SkyOcean.id("remove")
        val CODEC: MapCodec<RemovePatch> = SkyOceanCodecs.getMapCodec()
    }

    override fun id(): Identifier = ID
    override fun patch(jsonObject: JsonObject) {
        val parent = path.substringBeforeLast(".", "")
        val entry = path.substringAfterLast(".")
        jsonObject.getPath(parent, true)?.asJsonObject?.remove(entry)
    }
}
