package me.owdding.skyocean.helpers

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.teamresourceful.resourcefullib.common.codecs.CodecExtras
import com.teamresourceful.resourcefullib.common.codecs.EnumCodec
import net.minecraft.client.renderer.block.model.BlockModelDefinition
import net.minecraft.client.renderer.block.model.BlockStateModel
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import kotlin.jvm.optionals.getOrNull

class FakeBlockStateDefinition(
    val model: BlockModelDefinition,
    val blend: BlendMode?,
) {

    private var roots: Map<BlockState, BlockStateModel.UnbakedRoot>? = null

    fun getRoots(block: Block): Map<BlockState, BlockStateModel.UnbakedRoot> {
        if (roots == null) {
            roots = model.instantiate(block.stateDefinition) {
                block.builtInRegistryHolder()
                    .key()
                    //? if > 1.21.10 {
                    .identifier()
                    //?} else
                    /*.location()*/
                    .toString()
            }
        }
        return roots!!
    }

    fun instantiate(block: Block, baker: ModelBaker): Map<BlockState, BlockStateModel> = getRoots(block).mapValues { (state, model) ->
        model.bake(state, baker)
    }

    companion object {

        val CODEC: Codec<FakeBlockStateDefinition> = RecordCodecBuilder.create {
            it.group(
                MapCodec.assumeMapUnsafe(BlockModelDefinition.CODEC).forGetter(FakeBlockStateDefinition::model),
                EnumCodec.of(BlendMode::class.java).optionalFieldOf("blend").forGetter(CodecExtras.optionalFor(FakeBlockStateDefinition::blend)),
            ).apply(it) { model, mode -> FakeBlockStateDefinition(model, mode.getOrNull()) }
        }
    }
}
