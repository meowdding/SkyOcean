package codes.cookies.skyocean.helpers

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

class SelectBakedModel(val model: BlockStateModel): FabricBlockStateModel by model, BlockStateModel {

    override fun emitQuads(
        emitter: QuadEmitter,
        blockView: BlockAndTintGetter,
        pos: BlockPos,
        state: BlockState,
        random: RandomSource,
        cullTest: Predicate<Direction?>,
    ) {
        if ((pos.x % 2).or(pos.z % 2) == 0) {
            //alt.emitQuads(emitter, blockView, pos, state, random, cullTest)
            return
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
