package me.owdding.skyocean.features.garden.mutations

import com.google.gson.JsonObject
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity

@Module
object MutationData {
}

@GenerateCodec
data class Mutation(
    val name: String,
    val id: String,
    val rarity: SkyBlockRarity,
    val texture: JsonObject,
    val size: String,
    val surface: String,
    @FieldName("spreading_conditions") val spreadingConditions: JsonObject,
    val blueprint: JsonObject,
)
