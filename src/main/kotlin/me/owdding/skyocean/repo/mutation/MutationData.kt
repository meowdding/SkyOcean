package me.owdding.skyocean.repo.mutation

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.EnumCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.repo.models.AlternatingModel
import me.owdding.skyocean.repo.models.CustomModels
import me.owdding.skyocean.repo.models.SkyOceanBlockModel
import me.owdding.skyocean.repo.models.SkyOceanModel
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.lookup
import me.owdding.skyocean.utils.codecs.CodecHelpers
import net.minecraft.commands.arguments.blocks.BlockStateParser
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import org.joml.Vector3i
import org.joml.Vector3ic
import org.joml.div
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import kotlin.math.max
import kotlin.math.min

@Module
data object MutationData {

    val mutations: List<MutationEntry> = Utils.loadRepoData("mutations", CodecHelpers.list())

}

@GenerateCodec
data class MutationEntry(
    val name: String,
    val id: SkyBlockId,
    val texture: MutationTexture,
    val rarity: SkyBlockRarity,
    val size: MutationSize,
    @Compact val surface: List<Identifier>,
    @FieldName("spreading_conditions") val spreadingCondition: Map<Identifier, Int>,
    val blueprint: MutationBlueprint?,
)

data class MutationBlueprint(
    val map: Map<Vector3ic, SkyOceanModel>,
    val set: Set<SkyOceanModel>,
) {
    val min = map.keys.reduce { a, b ->
        Vector3i(
            min(a.x(), b.x()),
            min(a.y(), b.y()),
            min(a.z(), b.z()),
        )
    }
    val max = map.keys.reduce { a, b ->
        Vector3i(
            max(a.x(), b.x()),
            max(a.y(), b.y()),
            max(a.z(), b.z()),
        )
    }

    fun tick() {
        set.forEach(SkyOceanModel::tick)
    }

    companion object : MeowddingLogger by SkyOcean.featureLogger("mutation_blueprint") {
        @IncludedCodec(named = "mutation§blueprint")
        val paletteCodec: Codec<Map<String, List<String>>> = Codec.unboundedMap(Codec.STRING, CodecUtils.compactList(Codec.STRING))

        @IncludedCodec
        val codec: Codec<MutationBlueprint> = SkyOceanCodecs.CompletableMutationBlueprintCodec.codec().xmap(
            {
                val map = mutableMapOf<Vector3ic, SkyOceanModel>()
                val palette = it.palette.mapValues { (_, values) ->
                    val list = values.map {
                        runCatching {
                            if (it.startsWith("skyocean:")) {
                                return@map CustomModels.models.getOrElse(it.substringAfter(":")) {
                                    warn("Expected custom model ${it.substringAfter(":")}")
                                    SkyOceanBlockModel.EMPTY
                                }
                            }

                            SkyOceanBlockModel.single(BlockStateParser.parseForBlock(Registries.BLOCK.lookup(), it, true).blockState)
                        }.getOrElse { throwable ->
                            warn("Failed to parse blueprint placeholder $it", throwable)
                            SkyOceanBlockModel.EMPTY
                        }
                    }

                    if (list.size == 1) {
                        list[0]
                    } else {
                        AlternatingModel(list)
                    }
                }

                val halfSize = it.size / 2
                for (y in 0 until it.size.y()) {
                    val layer = it.shape[it.size.y() - 1 - y]
                    for (x in 0 until it.size.x()) {
                        val row = layer[x]
                        for (z in 0 until it.size.z()) {
                            val placeholder = row[z].toString()
                            if (placeholder == " ") continue
                            val blocks = palette[placeholder] ?: run {
                                warn("Unknown placeholder $placeholder!")
                                SkyOceanBlockModel.EMPTY
                            }

                            map[Vector3i(x - halfSize.x, y, z - halfSize.z)] = blocks
                        }
                    }
                }

                MutationBlueprint(map, palette.values.toSet())
            },
            { TODO() },
        )
    }

    @GenerateCodec
    @NamedCodec("CompletableMutationBlueprint")
    data class Completable(
        @NamedCodec("mutation§blueprint") val palette: Map<String, List<String>>,
        val shape: List<List<String>>,
        val size: Vector3ic,
    )
}

enum class MutationSize(val sizeX: Int, val sizeY: Int) {
    ONE_BY_ONE(1, 1)
    ;

    companion object {
        @IncludedCodec
        val CODEC: Codec<MutationSize> = Codec.withAlternative(
            EnumCodec.forKCodec(entries.toTypedArray()),
            Codec.STRING.flatXmap(
                {
                    val result = when (it) {
                        "1x1" -> ONE_BY_ONE
                        else -> null
                    }
                    if (result == null) DataResult.error { "Unable to parse $result" } else DataResult.success(result)
                },
                { DataResult.success(it.name) },
            ),
        )
    }
}

@GenerateCodec
data class MutationTexture(
    val value: String,
    val signature: String,
)
