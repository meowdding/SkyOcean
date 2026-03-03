package me.owdding.skyocean.helpers

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel
import net.minecraft.client.renderer.block.model.BlockModelPart
import net.minecraft.client.renderer.block.model.BlockStateModel
import net.minecraft.client.renderer.chunk.ChunkSectionLayer
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.client.resources.model.ResolvableModel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Predicate

enum class BlendMode {
    DEFAULT,
    SOLID,
    CUTOUT_MIPPED,
    CUTOUT,
    TRANSLUCENT,
    ;

    fun toSectionLayer(): ChunkSectionLayer? = when (this) {
        DEFAULT -> null
        SOLID -> ChunkSectionLayer.SOLID
        CUTOUT_MIPPED -> /*? if > 1.21.10 {*/ ChunkSectionLayer.CUTOUT /*?} else {*/ /*ChunkSectionLayer.CUTOUT_MIPPED *//*?}*/
        CUTOUT -> ChunkSectionLayer.CUTOUT
        TRANSLUCENT -> ChunkSectionLayer.TRANSLUCENT
    }
}

data class FakeBlockModelEntry(
    val blend: BlendMode? = null,
    val models: Map<BlockState, BlockStateModel>,
    val predicate: (BlockState, BlockPos) -> Boolean,
) {

    val transform: QuadTransform by lazy {
        if (blend != null) {
            QuadTransform { quad ->
                quad.renderLayer(blend.toSectionLayer())
                true
            }
        } else {
            QuadTransform { true }
        }
    }

    fun isActive(state: BlockState, pos: BlockPos): Boolean = predicate(state, pos)
}

class FakeBlockModel(
    val model: BlockStateModel,
    val alternatives: List<FakeBlockModelEntry>,
) : FabricBlockStateModel by model as FabricBlockStateModel, BlockStateModel {

    override fun emitQuads(
        emitter: QuadEmitter,
        blockView: BlockAndTintGetter,
        pos: BlockPos,
        state: BlockState,
        random: RandomSource,
        cullTest: Predicate<Direction?>,
    ) {
        if (alternatives.isNotEmpty()) {
            for (entry in alternatives) {
                val stateModel = entry.models[state] as? FabricBlockStateModel
                if (stateModel != null && entry.isActive(state, pos)) {
                    emitter.pushTransform(entry.transform)
                    stateModel.emitQuads(emitter, blockView, pos, state, random, cullTest)
                    emitter.popTransform()
                    return
                }
            }
        }

        (model as FabricBlockStateModel).emitQuads(emitter, blockView, pos, state, random, cullTest)
    }

    override fun collectParts(randomSource: RandomSource, list: MutableList<BlockModelPart>) {
        model.collectParts(randomSource, list)
    }

    override fun particleIcon(): TextureAtlasSprite = model.particleIcon()
}

class FakeBlockUnbakedModel(
    val block: Block,
    val original: BlockStateModel.UnbakedRoot,
    val entries: List<FakeBlockUnbakedEntry>,
) : BlockStateModel.UnbakedRoot {

    override fun bake(state: BlockState, baker: ModelBaker): BlockStateModel = FakeBlockModel(
        original.bake(state, baker),
        entries.map { (definition, predicate) ->
            FakeBlockModelEntry(
                definition.blend,
                definition.instantiate(state.block, baker),
                predicate,
            )
        },
    )

    override fun visualEqualityGroup(blockState: BlockState): Any? = original.visualEqualityGroup(blockState)

    override fun resolveDependencies(resolver: ResolvableModel.Resolver) {
        original.resolveDependencies(resolver)
        for ((definition, _) in entries) {
            for (root in definition.getRoots(this.block).values) {
                root.resolveDependencies(resolver)
            }
        }
    }

}
