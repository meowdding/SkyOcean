package me.owdding.skyocean.utils.rendering

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import org.joml.Matrix3x2f
import org.joml.Vector2i

object InventoryRenderer {

    val INVENTORY_BACKGROUND = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyOcean.id("inventory"))
            .withVertexShader(SkyOcean.id("core/inventory"))
            .withFragmentShader(SkyOcean.id("core/inventory"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform(POLY_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .build(),
    )
    val MONO_INVENTORY_BACKGROUND: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyOcean.id("mono_inventory"))
            .withVertexShader(SkyOcean.id("core/inventory"))
            .withFragmentShader(SkyOcean.id("core/mono_inventory"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform(MONO_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .build(),
    )


    fun renderMonoInventory(graphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int, size: Int, orientation: Orientation, color: Int) {
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
    }

    fun renderNormalInventory(graphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int, columns: Int, rows: Int, color: Int) {
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
    }
}
