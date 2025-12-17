package me.owdding.skyocean.features.garden.mutations

import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import me.owdding.lib.rendering.MeowddingPipState
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Vec3i
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3x2f
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import java.util.function.Function

class GuiBlockRenderer(buffer: MultiBufferSource.BufferSource) : PictureInPictureRenderer<GuiBlockRenderState>(buffer) {

    override fun getRenderStateClass(): Class<GuiBlockRenderState> = GuiBlockRenderState::class.java

    override fun renderToTexture(renderState: GuiBlockRenderState, poseStack: PoseStack) {
        poseStack.pushPop {
            with(renderState) {
                poseStack.scale(scale, -scale, -scale)

                poseStack.mulPose(Axis.XP.rotationDegrees(rotation.x.toFloat()))
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation.y.toFloat()))
                poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.z.toFloat()))

                val bufferSource = McClient.self.renderBuffers().bufferSource()

                blocks.forEach { (pos, blockState) ->
                    poseStack.pushPop {
                        poseStack.translate(
                            pos.x.toFloat(),
                            pos.y.toFloat(),
                            pos.z.toFloat()
                        )

                        poseStack.translate(-0.5f, -0.5f, -0.5f)

                        McClient.self.gameRenderer.lighting.setupFor(Lighting.Entry.ITEMS_FLAT)
                        McClient.self.blockRenderer.renderSingleBlock(
                            blockState,
                            poseStack,
                            bufferSource,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                        )
                    }
                }
                bufferSource.endBatch()
            }
        }
    }

    override fun getTextureLabel(): String = "skyocean_gui_block_renderer"
    override fun getTranslateY(height: Int, guiScale: Int): Float = height * 0.6f
    override fun textureIsReadyToBlit(renderState: GuiBlockRenderState) = true
}

data class GuiBlockRenderState(
    val blocks: Map<Vec3i, BlockState>,
    val rotation: Vec3 = Vec3(22.5, 45.0, 0.0),
    override val scale: Float = 20f,
    override val bounds: ScreenRectangle,
    override val scissorArea: ScreenRectangle?,
    override val pose: Matrix3x2f,
) : MeowddingPipState<GuiBlockRenderState>() {
    override fun getFactory(): Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<GuiBlockRenderState>> =
        Function { buffer -> GuiBlockRenderer(buffer) }

    override val shrinkToScissor: Boolean
        get() = false

    override val x0: Int = bounds.left()
    override val x1: Int = bounds.right()
    override val y0: Int = bounds.top()
    override val y1: Int = bounds.bottom()
}
