package me.owdding.skyocean.utils.rendering

//? >= 26.2
import com.mojang.blaze3d.PrimitiveTopology
//? >= 26.2
import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
//? 26.1
//import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.GuiGraphicsExtractor
//? 26.1
//import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderPipelines.register
import org.joml.Matrix3x2f
import org.joml.Vector2i

object InventoryRenderer {

    val INVENTORY_BACKGROUND = register(
        RenderPipeline.builder()
            .withLocation(SkyOcean.id("inventory"))
            .withVertexShader(SkyOcean.id("core/inventory"))
            .withFragmentShader(SkyOcean.id("core/inventory"))
            .withCull(false)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
            //? >= 26.2 {
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .withBindGroupLayout(BindGroupLayout.builder()
                .withUniform(POLY_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withSampler("Sampler0")
                .build())
            //?} else {
            /*.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform(POLY_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
             *///?}
            .build(),
    )
    val MONO_INVENTORY_BACKGROUND: RenderPipeline = register(
        RenderPipeline.builder()
            .withLocation(SkyOcean.id("mono_inventory"))
            .withVertexShader(SkyOcean.id("core/inventory"))
            .withFragmentShader(SkyOcean.id("core/mono_inventory"))
            .withCull(false)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
            //? >= 26.2 {
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .withBindGroupLayout(BindGroupLayout.builder()
                .withUniform(MONO_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withSampler("Sampler0")
                .build())
            //?} else {
            /*.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform(MONO_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
             *///?}
            .build(),
    )


    fun renderMonoInventory(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, size: Int, orientation: Orientation, color: Int) {
        graphics.guiRenderState.addPicturesInPictureState(
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

    fun renderNormalInventory(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, columns: Int, rows: Int, color: Int) {
        graphics.guiRenderState.addPicturesInPictureState(
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
