package me.owdding.skyocean.helpers.fakeblocks

import net.minecraft.client.renderer.block.model.BlockModelDefinition
import net.minecraft.client.renderer.block.model.BlockStateModel
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class FakeBlockStateDefinition(
    val model: BlockModelDefinition,
//     val blend: BlendMode?
) {

    private var roots: Map<BlockState, BlockStateModel.UnbakedRoot>? = null

    fun getRoots(block: Block): Map<BlockState, BlockStateModel.UnbakedRoot> {
        if (roots == null) {
            roots = model.instantiate(block.stateDefinition) { block.builtInRegistryHolder().key().location().toString() }
        }
        return roots!!
    }

    fun instantiate(block: Block, baker: ModelBaker): Map<BlockState, BlockStateModel> {
        return getRoots(block).mapValues { (state, model) -> model.bake(state, baker) }
    }

    companion object {

//         val CODEC: Codec<FakeBlockStateDefinition> = RecordCodecBuilder.create { it.group(
//             MapCodec.assumeMapUnsafe(BlockModelDefinition.CODEC).forGetter(FakeBlockStateDefinition::model),
//             EnumCodec.of(BlendMode::class.java).optionalFieldOf("blend").forGetter(CodecExtras.optionalFor(FakeBlockStateDefinition::blend))
//         ).apply(it) { model, mode -> FakeBlockStateDefinition(model, mode.getOrNull()) } }
    }
}
