package me.owdding.skyocean.utils.rendering

import com.mojang.renderpearl.api.pipeline.BindGroupLayout
import com.mojang.renderpearl.api.pipeline.RenderPipeline
import com.mojang.renderpearl.api.pipeline.UniformType
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderPipelines.register
import org.joml.Matrix3x2f
import org.joml.Vector2i

object InventoryRenderer {

    val INVENTORY_BACKGROUND = register(
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(SkyOcean.id("inventory"))
            .withFragmentShader(SkyOcean.id("core/inventory"))
            .withCull(false)
            //? < 26.3
            //.withShaderDefine("NO_LAYOUT")
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withUniform(POLY_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
                    .build(),
            )
            .build(),
    )

    val MONO_INVENTORY_BACKGROUND: RenderPipeline = register(
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(SkyOcean.id("mono_inventory"))
            .withFragmentShader(SkyOcean.id("core/mono_inventory"))
            .withCull(false)
            //? < 26.3
            //.withShaderDefine("NO_LAYOUT")
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withUniform(MONO_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
                    .build(),
            )
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
