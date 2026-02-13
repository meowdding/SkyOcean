package me.owdding.skyocean.repo.models

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector3f
import org.joml.Vector3fc
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import tech.thatgravyboat.skyblockapi.utils.extentions.scaled

@GenerateCodec
data class ModelTransform(
    val offset: Vector3fc = Vector3f(),
    val pivot: Vector3fc = Vector3f(),
    val leftRotation: Quaternionfc = Quaternionf(),
    val scale: Vector3fc = Vector3f(1f),
    val rightRotation: Quaternionfc = Quaternionf(),
) {
    companion object {
        val DEFAULT = ModelTransform()
    }
}

interface SkyOceanModel {

    val codec: MapCodec<out SkyOceanModel>

    val transform: ModelTransform get() = DEFAULT
    fun render(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        val (offset, pivot, leftRotation, scale, rightRotation) = transform
        poseStack.pushPop {
            rotateAround(leftRotation, pivot.x(), pivot.y(), pivot.z())
            scale(scale.x(), scale.y(), scale.z())
            rotateAround(rightRotation, pivot.x(), pivot.y(), pivot.z())
            translate(offset.x(), offset.y(), offset.z())
            emit(poseStack, bufferSource, packedLight, packedOverlay)
        }
    }

    fun tick()

    fun emit(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int)
}

@GenerateCodec
data class CompositeModel(
    val models: List<SkyOceanModel>,
    override val transform: ModelTransform = ModelTransform.DEFAULT
) : SkyOceanModel {
    override val codec: MapCodec<CompositeModel> = SkyOceanCodecs.getMapCodec()

    override fun tick() = models.forEach(SkyOceanModel::tick)
    override fun emit(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        models.forEach {
            it.render(poseStack, bufferSource, packedLight, packedOverlay)
        }
    }
}

@GenerateCodec
data class AlternatingModel(
    val entries: List<SkyOceanModel>,
) : SkyOceanModel {
    var ticks: Int = 0
    override val codec: MapCodec<AlternatingModel> = SkyOceanCodecs.getMapCodec()

    override fun tick() {
        ticks++
        entries.forEach(SkyOceanModel::tick)
    }

    fun select() = entries[Math.floorMod(ticks / 20, entries.size)]
    override val transform: ModelTransform get() = select().transform
    override fun emit(
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int,
    ) {
        select().emit(poseStack, bufferSource, packedLight, packedOverlay)
    }
}

sealed interface SkyOceanBlockModel : SkyOceanModel {
    val state: BlockState

    override fun emit(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        poseStack.scaled(y = -1, z = -1) {
            McClient.self.blockRenderer.renderSingleBlock(
                state,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay,
            )
        }
    }

    companion object {
        @JvmStatic
        val EMPTY = SingleBlockSupplier(Blocks.AIR.defaultBlockState())

        @JvmStatic
        fun single(blockState: BlockState): SkyOceanBlockModel = SingleBlockSupplier(blockState)
    }

    @GenerateCodec
    data class SingleBlockSupplier(
        override val state: BlockState,
        override val transform: ModelTransform = ModelTransform.DEFAULT,
    ) : SkyOceanBlockModel {
        override val codec: MapCodec<SingleBlockSupplier> = SkyOceanCodecs.getMapCodec()
        override fun tick() {}
    }
}
