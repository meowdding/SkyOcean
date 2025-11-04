package me.owdding.skyocean.utils.rendering

import com.mojang.blaze3d.vertex.PoseStack
import me.owdding.lib.rendering.world.RenderTypes
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3
import net.msrandom.stub.Stub
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.platform.drawString
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.awt.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Stub
expect fun GuiGraphics.applyPostEffect(id: ResourceLocation)

@Stub
internal expect fun renderFace(
    poseStack: PoseStack,
    buffer: MultiBufferSource,
    direction: Direction,
    vec6: RenderUtils.Vec6f,
    color: Int,
)

object RenderUtils {

    fun RenderWorldEvent.renderBox(pos: BlockPos, color: UInt = 0xFFFFFFFFu) {
        val color = color.toInt()
        ShapeRenderer.addChainedFilledBoxVertices(
            poseStack,
            buffer.getBuffer(RenderTypes.BLOCK_FILL_TRIANGLE_THROUGH_WALLS),
            pos.x.toDouble(),
            pos.y.toDouble(),
            pos.z.toDouble(),
            pos.x.toDouble() + 1.0,
            pos.y.toDouble() + 1.0,
            pos.z.toDouble() + 1.0,
            ARGB.redFloat(color),
            ARGB.greenFloat(color),
            ARGB.blueFloat(color),
            ARGB.alphaFloat(color),
        )
    }

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
        val x = cameraPosition.x
        val y = cameraPosition.y
        val z = cameraPosition.z

        val scale = max((cameraPosition.distanceTo(position).toFloat() / 10).toDouble(), 1.0).toFloat() * 0.025f

        poseStack.pushPop {
            poseStack.translate(position.x - x + 0.5, position.y - y + 1.07f, position.z - z + 0.5)
            poseStack.mulPose(cameraRotation)
            poseStack.scale(scale, -scale, scale)
            val xOffset = if (center) -McFont.width(text) / 2.0f else 0.0f

            drawString(
                text = text,
                x = xOffset,
                y = 0.0f,
                color = Color.WHITE.rgb.toUInt(),
                dropShadow = false,
                displayMode = Font.DisplayMode.SEE_THROUGH,
                backgroundColor = 0u,
                light = LightTexture.FULL_BRIGHT,
            )
        }
    }

    fun drawSlotHighlightBack(graphics: GuiGraphics, slotX: Int, slotY: Int) {
        graphics.drawSprite(SLOT_HIGHLIGHT_BACK_SPRITE, slotX - 4, slotY - 4, 24, 24)
    }

    fun drawSlotHighlightFront(graphics: GuiGraphics, slotX: Int, slotY: Int) {
        graphics.drawSprite(SLOT_HIGHLIGHT_FRONT_TEXTURE, slotX - 4, slotY - 4, 24, 24)
    }

    private val SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_back")
    private val SLOT_HIGHLIGHT_FRONT_TEXTURE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front")

    data class Vec6f(var a: Float, var b: Float, var c: Float, var d: Float, var e: Float, var f: Float)

    fun RenderWorldEvent.renderPlane(
        direction: Direction,
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        z: Int,
        color: Int,
    ) = renderPlane(direction, startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), z.toFloat(), color)

    fun RenderWorldEvent.renderPlane(
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
        renderFace(
            poseStack,
            buffer,
            direction,
            vec6,
            color,
        )
    }

    fun RenderWorldEvent.renderCylinder(
        x: Float,
        y: Float,
        z: Float,
        radius: Float,
        height: Float = 0.1f,
        color: Int,
    ) {
        atCamera {
            translate(x, y, z)
            val buffer = buffer.getBuffer(RenderType.debugFilledBox())

            for (i in 0..360) {
                val rad = Math.toRadians(i.toDouble())
                val nextRad = Math.toRadians(i + 1.toDouble())

                val x1 = radius * cos(rad)
                val y1 = radius * sin(rad)

                val x2 = radius * cos(nextRad)
                val y2 = radius * sin(nextRad)

                buffer.addVertex(poseStack.last().pose(), x2.toFloat(), 0f, y2.toFloat()).setColor(color)
                buffer.addVertex(poseStack.last().pose(), x1.toFloat(), 0f, y1.toFloat()).setColor(color)
                buffer.addVertex(poseStack.last().pose(), x2.toFloat(), height, y2.toFloat()).setColor(color)
                buffer.addVertex(poseStack.last().pose(), x1.toFloat(), height, y1.toFloat()).setColor(color)
            }
        }
    }

    fun RenderWorldEvent.renderCircle(
        x: Float,
        y: Float,
        z: Float,
        radius: Float,
        color: Int,
    ) {
        atCamera {
            translate(x, y, z)
            val buffer = buffer.getBuffer(RenderType.debugFilledBox())

            for (i in 0..360) {
                val rad = Math.toRadians(i.toDouble())
                val nextRad = Math.toRadians(i + 1.toDouble())

                val x1 = radius * cos(rad)
                val y1 = radius * sin(rad)

                val x2 = radius * cos(nextRad)
                val y2 = radius * sin(nextRad)

                buffer.addVertex(poseStack.last().pose(), 0f, 0f, 0f).setColor(color)
                buffer.addVertex(poseStack.last().pose(), x1.toFloat(), 0f, y1.toFloat()).setColor(color)
                buffer.addVertex(poseStack.last().pose(), x2.toFloat(), 0f, y2.toFloat()).setColor(color)
            }
        }
    }
}
