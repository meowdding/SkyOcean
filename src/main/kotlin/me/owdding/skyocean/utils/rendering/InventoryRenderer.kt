package me.owdding.skyocean.utils.rendering

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.platform.LogicOp
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.TextureFormat
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import org.joml.Matrix3x2f
import org.joml.Vector2i
import java.util.OptionalDouble
import java.util.OptionalInt
import kotlin.reflect.KMutableProperty0
import kotlin.use

//? if 1.21.5 {
/*import earth.terrarium.olympus.client.pipelines.PipelineRenderer
import tech.thatgravyboat.skyblockapi.helpers.McClient

private data class MonoState(val width: Int, val height: Int, val size: Int, val orientation: Orientation, val color: Int)
private data class NormalState(val width: Int, val height: Int, val columns: Int, val rows: Int, val color: Int)
*///?}

object InventoryRenderer {

    //? if 1.21.5 {
    /*private val MONO_TEXTURE = SkyOcean.id("textures/gui/inventory/mono.png")
    private val POLY_TEXTURE = SkyOcean.id("textures/gui/inventory/poly.png")
    *///?}

    val INVENTORY_BACKGROUND = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyOcean.id("inventory"))
            .withVertexShader(SkyOcean.id("core/inventory"))
            .withFragmentShader(SkyOcean.id("core/inventory"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            //? if 1.21.5
            /*.withColorLogic(LogicOp.NONE)*/
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            //? if > 1.21.5 {
            .withUniform(POLY_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            //?} else
            /*.withUniform("Size", UniformType.VEC2)*/
            .build(),
    )
    val MONO_INVENTORY_BACKGROUND: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyOcean.id("mono_inventory"))
            .withVertexShader(SkyOcean.id("core/inventory"))
            .withFragmentShader(SkyOcean.id("core/mono_inventory"))
            .withCull(false)
            //? if 1.21.5 {
            /*.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withColorLogic(LogicOp.NONE)
            *///?} else
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            //? if > 1.21.5 {
            .withUniform(MONO_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            //?} else {
            /*.withUniform("Size", UniformType.INT)
            .withUniform("Vertical", UniformType.INT)
            *///?}
            .build(),
    )


    fun renderMonoInventory(graphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int, size: Int, orientation: Orientation, color: Int) {
        //? if > 1.21.5 {
        graphics.guiRenderState.submitPicturesInPictureState(
            MonoInventoryPipState(
                x,
                y,
                x + width,
                y + height,
                graphics.scissorStack.peek(),
                Matrix3x2f(graphics.pose()),
                size,
                color,
                orientation == Orientation.VERTICAL,
            ),
        )
        //?} else {
        
        /*val state = MonoState(width, height, size, orientation, color)
        if (lastMonoState != state || lastMonoTexture == null) {
            val texture = McClient.self.textureManager.getTexture(MONO_TEXTURE).texture
            cacheShaderToTexture("SkyOcean Mono Inventory", MONO_INVENTORY_BACKGROUND, InventoryRenderer::lastMonoTexture, width, height, color) {
                setUniform("Size", size)
                setUniform("Vertical", orientation.getValue(0, 1))
                bindSampler("Sampler0", texture)
            }
        }

        val texture = lastMonoTexture ?: error("Mono inventory texture not initialized")

        drawTexture(graphics, x, y, width, height, color, texture)
        *///?}
    }

    fun renderNormalInventory(graphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int, columns: Int, rows: Int, color: Int) {
        //? if > 1.21.5 {
        graphics.guiRenderState.submitPicturesInPictureState(
            PolyInventoryPipState(
                x,
                y,
                x + width,
                y + height,
                graphics.scissorStack.peek(),
                Matrix3x2f(graphics.pose()),
                Vector2i(columns, rows),
                color,
            ),
        )
        //?} else {
        
        /*val state = NormalState(width, height, columns, rows, color)
        if (lastNormalState != state || lastNormalTexture == null) {
            val texture = McClient.self.textureManager.getTexture(POLY_TEXTURE).texture
            cacheShaderToTexture("SkyOcean Normal Inventory", INVENTORY_BACKGROUND, InventoryRenderer::lastNormalTexture, width, height, color) {
                setUniform("Size", columns.toFloat(), rows.toFloat())
                bindSampler("Sampler0", texture)
            }
        }

        val texture = lastNormalTexture ?: error("Normal inventory texture not initialized")

        drawTexture(graphics, x, y, width, height, color, texture)
        *///?}
    }

    //? if 1.21.5 {
    /*private var lastMonoState: MonoState? = null
    private var lastMonoTexture: GpuTexture? = null

    private var lastNormalState: NormalState? = null
    private var lastNormalTexture: GpuTexture? = null

    private inline fun cacheShaderToTexture(
        name: String,
        pipeline: RenderPipeline,
        property: KMutableProperty0<GpuTexture?>,
        width: Int,
        height: Int,
        color: Int,
        setup: RenderPass.() -> Unit,
    ) {
        RenderSystem.assertOnRenderThread()

        ByteBufferBuilder(4 * DefaultVertexFormat.POSITION_TEX_COLOR.vertexSize).use { builder ->

            val buffer = BufferBuilder(builder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
            buffer.addVertex(-1f, -1f, 0f).setUv(0f, 0f).setColor(color)
            buffer.addVertex(-1f, 1f, 0f).setUv(0f, 1f).setColor(color)
            buffer.addVertex(1f, 1f, 0f).setUv(1f, 1f).setColor(color)
            buffer.addVertex(1f, -1f, 0f).setUv(1f, 0f).setColor(color)

            val mesh = buffer.buildOrThrow()

            val device = RenderSystem.getDevice()
            val storageBuffer = RenderSystem.getSequentialBuffer(mesh.drawState().mode())
            val vertexBuffer = pipeline.vertexFormat.uploadImmediateVertexBuffer(mesh.vertexBuffer())
            val indexBuffer = storageBuffer.getBuffer(mesh.drawState().indexCount())

            val texture = RenderSystem.getDevice().createTexture(name, TextureFormat.RGBA8, width, height, 1)
            texture.setTextureFilter(FilterMode.NEAREST, false)

            property.get()?.close()
            property.set(texture)

            device.createCommandEncoder().createRenderPass(texture, OptionalInt.empty(), null, OptionalDouble.empty()).use { pass ->
                setup.invoke(pass)

                pass.setPipeline(pipeline)
                pass.setVertexBuffer(0, vertexBuffer)
                pass.setIndexBuffer(indexBuffer, storageBuffer.type())

                pass.drawIndexed(0, mesh.drawState().indexCount())
            }

            mesh.close()
        }
    }

    private fun drawTexture(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Int = -1,
        texture: GpuTexture,
    ) {
        val matrix = graphics.pose().last().pose()
        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
        buffer.addVertex(matrix, (x).toFloat(), (y).toFloat(), 1.0f).setUv(0f, 0f).setColor(color)
        buffer.addVertex(matrix, (x).toFloat(), (y + height).toFloat(), 1.0f).setUv(0f, 1f).setColor(color)
        buffer.addVertex(matrix, (x + width).toFloat(), (y + height).toFloat(), 1.0f).setUv(1f, 1f).setColor(color)
        buffer.addVertex(matrix, (x + width).toFloat(), (y).toFloat(), 1.0f).setUv(1f, 0f).setColor(color)

        RenderSystem.setShaderTexture(0, texture)
        PipelineRenderer.draw(RenderPipelines.GUI_TEXTURED, buffer.buildOrThrow()) {}
    }
    *///?}


}
