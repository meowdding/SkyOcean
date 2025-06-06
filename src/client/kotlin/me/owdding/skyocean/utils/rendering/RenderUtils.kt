package me.owdding.skyocean.utils.rendering

import me.owdding.skyocean.events.RenderWorldEvent
import me.owdding.skyocean.utils.rendering.RenderTypes.BLOCK_FILL_TRIANGLE
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.awt.Color
import kotlin.math.max

object RenderUtils {

    fun RenderWorldEvent.renderBox(pos: BlockPos, color: UInt = 0xFFFFFFFFu) {
        val color = color.toInt()
        ShapeRenderer.addChainedFilledBoxVertices(
            this.pose,
            this.buffer.getBuffer(BLOCK_FILL_TRIANGLE),
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

}
