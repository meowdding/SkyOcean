package me.owdding.skyocean.utils.rendering

//? >= 26.2 {
import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
//? }
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
//? 26.1 {
/*import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
*///? }
import earth.terrarium.olympus.client.pipelines.renderer.PipelineRenderer
import me.owdding.lib.rendering.MeowddingPipState
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
//? 26.1
//import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.SubmitNodeCollector
import org.joml.Matrix3x2f
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.helpers.McClient
//? 26.1
//import java.util.function.Function
import java.util.function.Supplier

val MONO_TEXTURE = SkyOcean.id("textures/gui/inventory/mono.png")
val POLY_TEXTURE = SkyOcean.id("textures/gui/inventory/poly.png")

data class MonoInventoryPipState(
    override val x0: Int,
    override val y0: Int,
    override val x1: Int,
    override val y1: Int,
    override val scissorArea: ScreenRectangle?,
    override val pose: Matrix3x2f,
    val size: Int,
    val color: Int,
    val vertical: Boolean,
) : MeowddingPipState<MonoInventoryPipState>() {
    override val shrinkToScissor: Boolean = false

    //? if >= 26.2 {
    override fun getFactory(): Supplier<PictureInPictureRenderer<MonoInventoryPipState>> = Supplier { MonoInventoryPipRenderer() }
    //? } else
    //override fun getFactory(): Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<MonoInventoryPipState>> = Function { buffer -> MonoInventoryPipRenderer(buffer) }
}

data class PolyInventoryPipState(
    override val x0: Int,
    override val y0: Int,
    override val x1: Int,
    override val y1: Int,
    override val scissorArea: ScreenRectangle?,
    override val pose: Matrix3x2f,
    val size: Vector2i,
    val color: Int,
) : MeowddingPipState<PolyInventoryPipState>() {
    override val shrinkToScissor: Boolean = false

    //? if >= 26.2 {
    override fun getFactory(): Supplier<PictureInPictureRenderer<PolyInventoryPipState>> = Supplier { PolyInventoryPipRenderer() }
    //? } else
    //override fun getFactory(): Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<PolyInventoryPipState>> = Function { buffer -> PolyInventoryPipRenderer(buffer) }
}

//~ if >= 26.2 '(buffer: MultiBufferSource.BufferSource) : ' -> '() : ', '(buffer)' -> '()'
class MonoInventoryPipRenderer() : PictureInPictureRenderer<MonoInventoryPipState>() {
    private var lastState: MonoInventoryPipState? = null

    override fun getRenderStateClass() = MonoInventoryPipState::class.java

    override fun textureIsReadyToBlit(state: MonoInventoryPipState): Boolean = lastState != null && lastState == state

    override fun renderToTexture(state: MonoInventoryPipState, stack: PoseStack/*? >= 26.2 >> ')'*/, submitNodeCollector: SubmitNodeCollector) {
        val bounds = state.bounds ?: return

        val scale = McClient.window.guiScale.toFloat()
        val scaledWidth = bounds.width * scale
        val scaledHeight = bounds.height * scale

        //? >= 26.2 {
        ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_TEX_COLOR.vertexSize * 4).use {
            val bufferBuilder = BufferBuilder(it, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
        //?} else
            //val bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)

            bufferBuilder.addVertex(0f, 0f, 0f).setUv(0f, 0f).setColor(state.color)
            bufferBuilder.addVertex(0f, scaledHeight, 0f).setUv(0f, 1f).setColor(state.color)
            bufferBuilder.addVertex(scaledWidth, scaledHeight, 0f).setUv(1f, 1f).setColor(state.color)
            bufferBuilder.addVertex(scaledWidth, 0f, 0f).setUv(1f, 0f).setColor(state.color)

            val texture = McClient.self.textureManager.getTexture(MONO_TEXTURE)

            PipelineRenderer.builder(InventoryRenderer.MONO_INVENTORY_BACKGROUND, bufferBuilder.buildOrThrow())
                .uniform(MonoInventoryUniform.STORAGE, MonoInventoryUniform(state.size, if (state.vertical) 1 else 0))
                .textures(TextureSetup.singleTexture(texture.textureView, texture.sampler))
                .color(state.color)
                .draw()

            this.lastState = state
            //? >= 26.2
        }

    }

    override fun getTextureLabel() = "skyocean_mono_inventory"

}

//~ if >= 26.2 '(buffer: MultiBufferSource.BufferSource) : ' -> '() : ', '(buffer)' -> '()'
class PolyInventoryPipRenderer() : PictureInPictureRenderer<PolyInventoryPipState>() {
    private var lastState: PolyInventoryPipState? = null

    override fun getRenderStateClass() = PolyInventoryPipState::class.java

    override fun textureIsReadyToBlit(state: PolyInventoryPipState): Boolean = lastState != null && lastState == state

    override fun renderToTexture(state: PolyInventoryPipState, stack: PoseStack/*? >= 26.2 >> ')'*/, submitNodeCollector: SubmitNodeCollector) {
        val bounds = state.bounds ?: return

        val scale = McClient.window.guiScale.toFloat()
        val scaledWidth = bounds.width * scale
        val scaledHeight = bounds.height * scale

        //? >= 26.2 {
        ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_TEX_COLOR.vertexSize * 4).use {
            val bufferBuilder = BufferBuilder(it, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
            //?} else
            //val bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
            bufferBuilder.addVertex(0f, 0f, 0f).setUv(0f, 0f).setColor(state.color)
            bufferBuilder.addVertex(0f, scaledHeight, 0f).setUv(0f, 1f).setColor(state.color)
            bufferBuilder.addVertex(scaledWidth, scaledHeight, 0f).setUv(1f, 1f).setColor(state.color)
            bufferBuilder.addVertex(scaledWidth, 0f, 0f).setUv(1f, 0f).setColor(state.color)

            val texture = McClient.self.textureManager.getTexture(POLY_TEXTURE)
            PipelineRenderer.builder(InventoryRenderer.INVENTORY_BACKGROUND, bufferBuilder.buildOrThrow())
                .uniform(PolyInventoryUniform.STORAGE, PolyInventoryUniform(state.size))
                .textures(TextureSetup.singleTexture(texture.textureView, texture.sampler))
                .color(state.color)
                .draw()

            this.lastState = state
        //? >= 26.2
        }
    }

    override fun getTextureLabel() = "skyocean_poly_inventory"

}
