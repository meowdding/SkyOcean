package codes.cookies.skyocean.helpers.fakeblocks

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.teamresourceful.resourcefullib.common.codecs.CodecExtras
import com.teamresourceful.resourcefullib.common.codecs.EnumCodec
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.minecraft.client.renderer.block.model.BlockModelDefinition
import net.minecraft.client.renderer.block.model.BlockStateModel
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import kotlin.jvm.optionals.getOrNull

class FakeBlockStateDefinition(
    val model: BlockModelDefinition,
    val blend: BlendMode?
) {

    fun instantiate(state: StateDefinition<Block, BlockState>, baker: ModelBaker, id: String): Map<BlockState, BlockStateModel> {
        return model.instantiate(state) { id }.mapValues { (state, model) -> model.bake(state, baker) }
    }

    companion object {

        val CODEC: Codec<FakeBlockStateDefinition> = RecordCodecBuilder.create { it.group(
            MapCodec.assumeMapUnsafe(BlockModelDefinition.CODEC).forGetter(FakeBlockStateDefinition::model),
            EnumCodec.of(BlendMode::class.java).optionalFieldOf("blend").forGetter(CodecExtras.optionalFor(FakeBlockStateDefinition::blend))
        ).apply(it) { model, mode -> FakeBlockStateDefinition(model, mode.getOrNull()) } }
    }
}
