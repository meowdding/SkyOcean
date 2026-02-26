package me.owdding.skyocean.helpers

import com.google.gson.JsonParser
import com.mojang.logging.LogUtils
import com.mojang.serialization.JsonOps
import me.owdding.skyocean.events.RegisterFakeBlocksEvent
import me.owdding.skyocean.utils.LateInitModule
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin
import net.minecraft.core.BlockPos
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

typealias FakeBlockEntry = Pair<Identifier, (BlockState, BlockPos) -> Boolean>
typealias FakeBlockUnbakedEntry = Pair<FakeBlockStateDefinition, (BlockState, BlockPos) -> Boolean>

@LateInitModule
object FakeBlocks : PreparableModelLoadingPlugin<Map<Identifier, FakeBlockStateDefinition>> {

    private val logger = LogUtils.getLogger()
    private val path = FileToIdConverter.json(BLOCK_STATES_PATH)

    val fakeBlocks = mutableMapOf<Block, MutableList<FakeBlockEntry>>()

    init {
        PreparableModelLoadingPlugin.register(FakeBlocks::init, FakeBlocks)
    }

    @Suppress("unused")
    private fun register(
        block: Block,
        texture: Block,
        definition: Identifier,
        parent: Identifier?,
        predicate: (BlockState, BlockPos) -> Boolean,
    ) {
        fakeBlocks.getOrPut(block, ::mutableListOf).add(FakeBlockEntry(definition, predicate))
    }

    fun init(manager: PreparableReloadListener.SharedState, executor: Executor): CompletableFuture<Map<Identifier, FakeBlockStateDefinition>> {
        val manager = manager.resourceManager()
        fakeBlocks.clear()
        RegisterFakeBlocksEvent(this::register).post(SkyBlockAPI.eventBus)

        return CompletableFuture.supplyAsync(
            {
                val output = mutableMapOf<Identifier, FakeBlockStateDefinition>()

                for ((file, resource) in path.listMatchingResources(manager)) {
                    runCatching {
                        val definition = resource.openAsReader().use { reader ->
                            FakeBlockStateDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).orThrow
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

    override fun initialize(definitions: Map<Identifier, FakeBlockStateDefinition>, context: ModelLoadingPlugin.Context) {
        context.modifyBlockModelOnLoad().register { original, context ->
            val block = context.state().block

            fakeBlocks[block]?.let { entries ->
                val unbakedEntries = mutableListOf<FakeBlockUnbakedEntry>()
                for (entry in entries) {
                    val (id, predicate) = entry
                    val definition = definitions[id] ?: continue
                    unbakedEntries.add(FakeBlockUnbakedEntry(definition, predicate))
                }

                FakeBlockUnbakedModel(block, original, unbakedEntries)
            } ?: original
        }
    }

}
