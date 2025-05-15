package me.owdding.skyocean.helpers.fakeblocks

import com.google.gson.JsonParser
import com.mojang.logging.LogUtils
import me.owdding.skyocean.events.RegisterFakeBlocksEvent
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin
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

typealias FakeBlockEntry = Pair<ResourceLocation, (BlockState, BlockPos) -> Boolean>
typealias FakeBlockUnbakedEntry = Pair<FakeBlockStateDefinition, (BlockState, BlockPos) -> Boolean>

const val BLOCK_STATES_PATH = "virtualblockstates"

object FakeBlocks : PreparableModelLoadingPlugin<Map<ResourceLocation, FakeBlockStateDefinition>> {

    private val logger = LogUtils.getLogger()
    private val path = FileToIdConverter.json(BLOCK_STATES_PATH)

    val fakeBlocks = mutableMapOf<Block, MutableList<FakeBlockEntry>>()

    private fun register(
        block: Block,
        definition: ResourceLocation,
        predicate: (BlockState, BlockPos) -> Boolean,
    ) {
        fakeBlocks.getOrPut(block, ::mutableListOf).add(FakeBlockEntry(definition, predicate))
    }

    fun init(manager: ResourceManager, executor: Executor): CompletableFuture<Map<ResourceLocation, FakeBlockStateDefinition>> {
        fakeBlocks.clear()
        RegisterFakeBlocksEvent(this::register).post(SkyBlockAPI.eventBus)

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

    override fun initialize(definitions: Map<ResourceLocation, FakeBlockStateDefinition>, context: ModelLoadingPlugin.Context) {
        context.modifyBlockModelOnLoad().register { original, context ->
            val block = context.state().block

            fakeBlocks[block]?.let { entries ->
                val unbakedEntries = mutableListOf<FakeBlockUnbakedEntry>()
                for (entry in entries) {
                    val (id, predicate) = entry
                    val definition = definitions[id]
                    definition ?: logger.error("Failed to load fake block state definition from $id")
                    definition ?: continue
                    unbakedEntries.add(FakeBlockUnbakedEntry(definition, predicate))
                }

                FakeBlockUnbakedModel(block, original, unbakedEntries)
            } ?: original
        }
    }

}
