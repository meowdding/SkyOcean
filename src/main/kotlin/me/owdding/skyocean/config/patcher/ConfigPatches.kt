package me.owdding.skyocean.config.patcher

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import java.nio.file.Files
import java.util.function.UnaryOperator
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText

object ConfigPatches {

    private val registry = mutableMapOf<ResourceLocation, MapCodec<out Patch>>()

    val CODEC: Codec<Patch> = ResourceLocation.CODEC.dispatch(Patch::id, registry::get)

    init {
        register(MovePatch.ID, MovePatch.CODEC)
        register(CompoundPatch.ID, CompoundPatch.CODEC)
        register(AddPatch.ID, SkyOceanCodecs.AddPatchCodec)
        register(AddListPatch.ID, SkyOceanCodecs.AddListPatchCodec)
    }

    fun register(id: ResourceLocation, codec: MapCodec<out Patch>) {
        registry[id] = codec
    }

    fun loadPatches(): Map<Int, UnaryOperator<JsonObject>> {
        val orElseThrow = SkyOcean.SELF.findPath("repo/patches").orElseThrow()
        val patches = mutableListOf<Pair<Int, UnaryOperator<JsonObject>>>()

        for (path in Files.walk(orElseThrow)) {
            if (Files.isRegularFile(path)) {
                val id = path.nameWithoutExtension.filter { c -> c.isDigit() }.toInt()
                val patcher = path.readText().readJson<JsonObject>().toDataOrThrow(CODEC)
                patches.add(id to patcher)
            }
        }
        return patches.sortedBy { it.first }.toMap()
    }

}
