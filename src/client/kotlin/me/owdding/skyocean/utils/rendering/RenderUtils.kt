package me.owdding.skyocean.utils.rendering

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.platform.LogicOp
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.pipelines.PipelineRenderer
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.events.RenderWorldEvent
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.awt.Color
import kotlin.math.max

object RenderUtils {

    fun RenderWorldEvent.renderTextInWorld(
        position: Vec3,
        text: String,
        center: Boolean,
    ) {
        renderTextInWorld(position, Text.of(text), center)
    }

    fun RenderWorldEvent.renderTextInWorld(
        position: Vec3,
        text: Component,
        center: Boolean,
    ) {
        val x = camera.position.x
        val y = camera.position.y
        val z = camera.position.z

        val scale = max((camera.position.distanceTo(position).toFloat() / 10).toDouble(), 1.0).toFloat() * 0.025f

        pose.pushPop {
            pose.translate(position.x - x + 0.5, position.y - y + 1.07f, position.z - z + 0.5)
            pose.mulPose(camera.rotation())
            pose.scale(scale, -scale, scale)
            val xOffset = if (center) -McFont.width(text) / 2.0f else 0.0f
            McFont.self.drawInBatch(
                text,
                xOffset,
                0.0f,
                Color.WHITE.rgb,
                false,
                pose.last().pose(),
                buffer,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LightTexture.FULL_BRIGHT,
            )
        }
    }

    fun drawSlotHighlightBack(graphics: GuiGraphics, slotX: Int, slotY: Int) {
        graphics.blitSprite(RenderType::guiTextured, SLOT_HIGHLIGHT_BACK_SPRITE, slotX - 4, slotY - 4, 24, 24)
    }

    fun drawSlotHighlightFront(graphics: GuiGraphics, slotX: Int, slotY: Int) {
        graphics.blitSprite(RenderType::guiTextured, SLOT_HIGHLIGHT_FRONT_TEXTURE, slotX - 4, slotY - 4, 24, 24)
    }

    private val SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_back")
    private val SLOT_HIGHLIGHT_FRONT_TEXTURE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front")

    private data class Vec6f(var a: Float, var b: Float, var c: Float, var d: Float, var e: Float, var f: Float)

    fun renderPlane(
        event: RenderWorldEvent,
        direction: Direction,
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        z: Int,
        color: Int,
    ) = renderPlane(event, direction, startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), z.toFloat(), color)

    fun renderPlane(
        event: RenderWorldEvent,
        direction: Direction,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        z: Float,
        color: Int,
    ) {
        val vec6 = when (direction) {
            Direction.UP, Direction.DOWN -> Vec6f(startX, z, startY, endX, z, endY)
            Direction.NORTH, Direction.SOUTH -> Vec6f(startX, startY, z, endX, endY, z)
            Direction.EAST, Direction.WEST -> Vec6f(z, startX, startY, z, endX, endY)
        }
        ShapeRenderer.renderFace(
            event.pose,
            event.buffer.getBuffer(RenderTypes.BLOCK_FILL),
            direction,
            vec6.a,
            vec6.b,
            vec6.c,
            vec6.d,
            vec6.e,
            vec6.f,
            ARGB.redFloat(color),
            ARGB.greenFloat(color),
            ARGB.blueFloat(color),
            ARGB.alphaFloat(color),
        )
    }


    val MONO_TEXTURE = SkyOcean.id("textures/gui/inventory/mono.png")
    val POLY_TEXTURE = SkyOcean.id("textures/gui/inventory/poly.png")

    val INVENTORY_BACKGROUND = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyOcean.id("inventory"))
            .withVertexShader(SkyOcean.id("core/inventory"))
            .withFragmentShader(SkyOcean.id("core/inventory"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withColorLogic(LogicOp.NONE)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            .withUniform("ProjMat", UniformType.MATRIX4X4)
            .withUniform("Size", UniformType.VEC2)
            .build(),
    )
    val MONO_INVENTORY_BACKGROUND = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyOcean.id("mono_inventory"))
            .withVertexShader(SkyOcean.id("core/inventory"))
            .withFragmentShader(SkyOcean.id("core/mono_inventory"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withColorLogic(LogicOp.NONE)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            .withUniform("ProjMat", UniformType.MATRIX4X4)
            .withUniform("Size", UniformType.INT)
            .withUniform("Vertical", UniformType.INT)
            .build(),
    )

    private fun drawBuffer(graphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int, color: Int): BufferBuilder {
        val matrix = graphics.pose().last().pose()
        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
        buffer.addVertex(matrix, (x).toFloat(), (y).toFloat(), 1.0f).setUv(0f, 0f).setColor(color)
        buffer.addVertex(matrix, (x).toFloat(), (y + height).toFloat(), 1.0f).setUv(0f, 1f).setColor(color)
        buffer.addVertex(matrix, (x + width).toFloat(), (y + height).toFloat(), 1.0f).setUv(1f, 1f).setColor(color)
        buffer.addVertex(matrix, (x + width).toFloat(), (y).toFloat(), 1.0f).setUv(1f, 0f).setColor(color)
        return buffer
    }

    fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        size: Int,
        orientation: Orientation,
        color: Int,
    ) {
        val gpuTexture: GpuTexture = McClient.self.textureManager.getTexture(MONO_TEXTURE).texture
        RenderSystem.setShaderTexture(0, gpuTexture)
        PipelineRenderer.draw(MONO_INVENTORY_BACKGROUND, drawBuffer(graphics, x, y, width, height, color).buildOrThrow()) { pass: RenderPass ->
            pass.bindSampler("Sampler0", gpuTexture)
            pass.setUniform("Size", size)
            pass.setUniform("Vertical", orientation.getValue(0, 1))
        }
    }

    fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        columns: Int,
        rows: Int,
        color: Int,
    ) {
        val gpuTexture: GpuTexture = McClient.self.textureManager.getTexture(POLY_TEXTURE).texture
        RenderSystem.setShaderTexture(0, gpuTexture)
        PipelineRenderer.draw(INVENTORY_BACKGROUND, drawBuffer(graphics, x, y, width, height, color).buildOrThrow()) { pass: RenderPass ->
            pass.bindSampler("Sampler0", gpuTexture)
            pass.setUniform("Size", columns.toFloat(), rows.toFloat())
        }
    }

}
