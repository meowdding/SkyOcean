package codes.cookies.skyocean.helpers.fakeblocks

import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel
import net.minecraft.client.renderer.block.model.BlockModelPart
import net.minecraft.client.renderer.block.model.BlockStateModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Predicate

data class FakeBlockModelEntry(
    val blend: BlendMode? = null,
    val models: Map<BlockState, BlockStateModel>,
    val predicate: (BlockState, BlockPos) -> Boolean,
) {

    val transform: QuadTransform by lazy {
        val materials = Renderer.get()?.materialFinder()
        if (blend != null && materials != null) {
            QuadTransform { quad ->
                quad.material(materials.copyFrom(quad.material()).blendMode(blend).find())
                true
            }
        } else {
            QuadTransform { true }
        }
    }

    fun isActive(state: BlockState, pos: BlockPos): Boolean {
        return predicate(state, pos)
    }
}

class FakeBlockModel(
    val model: BlockStateModel,
    val alternatives: List<FakeBlockModelEntry>
): FabricBlockStateModel by model, BlockStateModel {

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
                val stateModel = entry.models[state]
                if (stateModel != null && entry.isActive(state, pos)) {
                    emitter.pushTransform(entry.transform)
                    stateModel.emitQuads(emitter, blockView, pos, state, random, cullTest)
                    emitter.popTransform()
                    return
                }
            }
        }

        model.emitQuads(emitter, blockView, pos, state, random, cullTest)
    }

    override fun collectParts(randomSource: RandomSource, list: MutableList<BlockModelPart>) {
        model.collectParts(randomSource, list)
    }

    override fun particleIcon(): TextureAtlasSprite {
        return model.particleIcon()
    }
}
