package codes.cookies.skyocean.helpers.fakeblocks

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
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
        var base = model

        if (alternatives.isNotEmpty()) {
            for (entry in alternatives) {
                val (model, predicate) = entry
                val stateModel = model[state]
                if (stateModel != null && predicate.invoke(state, pos)) {
                    base = stateModel
                    break
                }
            }
        }

        base.emitQuads(emitter, blockView, pos, state, random, cullTest)
    }

    override fun collectParts(randomSource: RandomSource, list: MutableList<BlockModelPart>) {
        model.collectParts(randomSource, list)
    }

    override fun particleIcon(): TextureAtlasSprite {
        return model.particleIcon()
    }
}
