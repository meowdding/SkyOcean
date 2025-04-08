package codes.cookies.skyocean.helpers.fakeblocks

import codes.cookies.skyocean.events.FakeBlockModelEvent
import com.google.gson.JsonParser
import com.mojang.logging.LogUtils
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin
import net.minecraft.client.renderer.block.model.BlockModelPart
import net.minecraft.core.BlockPos
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

typealias FakeBlockModelEntry = Pair<Map<BlockState, BlockModelPart>, (BlockState, BlockPos) -> Boolean>
typealias FakeBlockEntry = Pair<ResourceLocation, (BlockState, BlockPos) -> Boolean>

object FakeBlocks : PreparableModelLoadingPlugin<Map<ResourceLocation, FakeBlockStateDefinition>> {

    private val logger = LogUtils.getLogger()
    private val path = FileToIdConverter.json("overwrite/blockstates")

    private val fakeBlocks = mutableMapOf<Block, MutableList<FakeBlockEntry>>()

    private fun register(
        block: Block,
        model: ResourceLocation,
        predicate: (BlockState, BlockPos) -> Boolean
    ) {
        fakeBlocks.getOrPut(block, ::mutableListOf).add(FakeBlockEntry(model, predicate))
    }

    fun init(manager: ResourceManager, executor: Executor): CompletableFuture<Map<ResourceLocation, FakeBlockStateDefinition>> {
        fakeBlocks.clear()
        FakeBlockModelEvent(this::register).post(SkyBlockAPI.eventBus)

        return CompletableFuture.supplyAsync<Map<ResourceLocation, FakeBlockStateDefinition>>(
            {
                val output = mutableMapOf<ResourceLocation, FakeBlockStateDefinition>()

                for ((file, resource) in path.listMatchingResources(manager)) {
                    runCatching {
                        val definition = resource.openAsReader()?.use { reader ->
                            JsonParser.parseReader(reader).toDataOrThrow(FakeBlockStateDefinition.CODEC)
                        }

                        if (definition != null) {
                            output[path.fileToId(file)] = definition
                        } else {
                            logger.error("Failed to load fake block state definition from $file")
                        }
                    }.getOrElse { exception ->
                        logger.error("Failed to load fake block state definition from $file", exception)
                    }
                }

                output
            },
            executor,
        )
    }

    override fun initialize(models: Map<ResourceLocation, FakeBlockStateDefinition>, context: ModelLoadingPlugin.Context) {
        context.modifyBlockModelAfterBake().register { original, context ->
            val block = context.state().block

            fakeBlocks[block]?.let { entries ->
                val modelEntries = mutableListOf<FakeBlockModelEntry>()
                for (entry in entries) {
                    val (model, predicate) = entry
                    val fakeModel = models[model]?.init(block, context.baker()) ?: error("Model $model not found")
                    modelEntries.add(FakeBlockModelEntry(fakeModel, predicate))
                }

                FakeBlockModel(original, modelEntries)
            } ?: original
        }
    }

}
