package me.owdding.skyocean.utils.rendering

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.pipelines.pips.OlympusPictureInPictureRenderState
import earth.terrarium.olympus.client.pipelines.renderer.PipelineRenderer
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.MultiBufferSource
import org.joml.Matrix3x2f
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.util.function.Function

val MONO_TEXTURE = SkyOcean.id("textures/gui/inventory/mono.png")
val POLY_TEXTURE = SkyOcean.id("textures/gui/inventory/poly.png")

abstract class SkyOceanPipState<T : OlympusPictureInPictureRenderState<T>>() : OlympusPictureInPictureRenderState<T> {
    abstract val x0: Int
    abstract val y0: Int
    abstract val x1: Int
    abstract val y1: Int

    open val scale: Float = 1f

    abstract val scissorArea: ScreenRectangle?
    abstract val pose: Matrix3x2f

    val bounds: ScreenRectangle? by lazy {
        if (scissorArea != null) {
            scissorArea!!.intersection(ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose))
        } else {
            ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose)
        }
    }

    override fun x0() = x0
    override fun y0() = y0
    override fun x1() = x1
    override fun y1() = y1
    override fun scissorArea() = scissorArea
    override fun pose() = pose
    override fun scale() = scale
    override fun bounds() = bounds
}

data class MonoInventoryPipState(
    override val x0: Int, override val y0: Int, override val x1: Int, override val y1: Int,
    override val scissorArea: ScreenRectangle?,
    override val pose: Matrix3x2f,
    val size: Int,
    val color: Int,
    val vertical: Boolean,
) : SkyOceanPipState<MonoInventoryPipState>() {
    override fun getFactory(): Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<MonoInventoryPipState>> =
        Function { buffer -> MonoInventoryPipRenderer(buffer) }
}

data class PolyInventoryPipState(
    override val x0: Int, override val y0: Int, override val x1: Int, override val y1: Int,
    override val scissorArea: ScreenRectangle?,
    override val pose: Matrix3x2f,
    val size: Vector2i,
    val color: Int,
) : SkyOceanPipState<PolyInventoryPipState>() {
    override fun getFactory(): Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<PolyInventoryPipState>> =
        Function { buffer -> PolyInventoryPipRenderer(buffer) }
}

class MonoInventoryPipRenderer(source: MultiBufferSource.BufferSource) : PictureInPictureRenderer<MonoInventoryPipState>(source) {
    private var lastState: MonoInventoryPipState? = null

    override fun getRenderStateClass() = MonoInventoryPipState::class.java

    override fun textureIsReadyToBlit(state: MonoInventoryPipState): Boolean {
        return lastState != null && lastState == state
    }

    override fun renderToTexture(state: MonoInventoryPipState, stack: PoseStack) {
        val bounds = state.bounds ?: return

        val scale = McClient.window.guiScale.toFloat()
        val scaledWidth = bounds.width * scale
        val scaledHeight = bounds.height * scale

        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
        buffer.addVertex(0f, 0f, 0f).setUv(0f, 0f).setColor(state.color)
        buffer.addVertex(0f, scaledHeight, 0f).setUv(0f, 1f).setColor(state.color)
        buffer.addVertex(scaledWidth, scaledHeight, 0f).setUv(1f, 1f).setColor(state.color)
        buffer.addVertex(scaledWidth, 0f, 0f).setUv(1f, 0f).setColor(state.color)

        RenderSystem.setShaderTexture(0, McClient.self.textureManager.getTexture(MONO_TEXTURE).textureView)

        PipelineRenderer.builder(InventoryRenderer.MONO_INVENTORY_BACKGROUND, buffer.buildOrThrow())
            .uniform(MonoInventoryUniform.STORAGE, MonoInventoryUniform(state.size, if (state.vertical) 1 else 0))
            .color(state.color)
            .draw()

        this.lastState = state
    }

    override fun getTextureLabel() = "skyocean_mono_inventory"

}

class PolyInventoryPipRenderer(source: MultiBufferSource.BufferSource) : PictureInPictureRenderer<PolyInventoryPipState>(source) {
    private var lastState: PolyInventoryPipState? = null

    override fun getRenderStateClass() = PolyInventoryPipState::class.java

    override fun textureIsReadyToBlit(state: PolyInventoryPipState): Boolean {
        return lastState != null && lastState == state
    }

    override fun renderToTexture(state: PolyInventoryPipState, stack: PoseStack) {
        val bounds = state.bounds ?: return

        val scale = McClient.window.guiScale.toFloat()
        val scaledWidth = bounds.width * scale
        val scaledHeight = bounds.height * scale

        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
        buffer.addVertex(0f, 0f, 0f).setUv(0f, 0f).setColor(state.color)
        buffer.addVertex(0f, scaledHeight, 0f).setUv(0f, 1f).setColor(state.color)
        buffer.addVertex(scaledWidth, scaledHeight, 0f).setUv(1f, 1f).setColor(state.color)
        buffer.addVertex(scaledWidth, 0f, 0f).setUv(1f, 0f).setColor(state.color)

        RenderSystem.setShaderTexture(0, McClient.self.textureManager.getTexture(POLY_TEXTURE).textureView)

        PipelineRenderer.builder(InventoryRenderer.INVENTORY_BACKGROUND, buffer.buildOrThrow())
            .uniform(PolyInventoryUniform.STORAGE, PolyInventoryUniform(state.size))
            .color(state.color)
            .draw()

        this.lastState = state
    }

    override fun getTextureLabel() = "skyocean_poly_inventory"

}
