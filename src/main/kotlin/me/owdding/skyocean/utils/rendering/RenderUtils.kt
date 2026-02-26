package me.owdding.skyocean.utils.rendering

//? if < 1.21.11
/*import com.mojang.blaze3d.systems.RenderSystem*/
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
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
import me.owdding.lib.rendering.world.RenderTypes as MLibRenderTypes


interface PostEffectApplicator {
    fun `skyocean$applyPostEffect`(id: Identifier)

    fun `skyocean$getPostEffect`(): Identifier?
}

fun GuiGraphics.applyPostEffect(id: Identifier) {
    this.nextStratum()
    (this.guiRenderState as? PostEffectApplicator)?.`skyocean$applyPostEffect`(id)
    this.fill(0, 0, this.guiWidth(), this.guiHeight(), 0)
}

internal fun renderFace(
    poseStack: PoseStack,
    buffer: MultiBufferSource,
    direction: Direction,
    vec6: RenderUtils.Vec6f,
    color: Int,
) {
    val matrix = poseStack.last().pose()
    val vertexconsumer = buffer.getBuffer(MLibRenderTypes.BLOCK_FILL_QUAD)

    val (a, b, c, d, e, f) = vec6

    when (direction) {
        Direction.DOWN -> {
            vertexconsumer.addVertex(matrix, a, b, c).setColor(color)
            vertexconsumer.addVertex(matrix, d, b, c).setColor(color)
            vertexconsumer.addVertex(matrix, d, b, f).setColor(color)
            vertexconsumer.addVertex(matrix, a, b, f).setColor(color)
        }

        Direction.UP -> {
            vertexconsumer.addVertex(matrix, a, e, c).setColor(color)
            vertexconsumer.addVertex(matrix, a, e, f).setColor(color)
            vertexconsumer.addVertex(matrix, d, e, f).setColor(color)
            vertexconsumer.addVertex(matrix, d, e, c).setColor(color)
        }

        Direction.NORTH -> {
            vertexconsumer.addVertex(matrix, a, b, c).setColor(color)
            vertexconsumer.addVertex(matrix, a, e, c).setColor(color)
            vertexconsumer.addVertex(matrix, d, e, c).setColor(color)
            vertexconsumer.addVertex(matrix, d, b, c).setColor(color)
        }

        Direction.SOUTH -> {
            vertexconsumer.addVertex(matrix, a, b, f).setColor(color)
            vertexconsumer.addVertex(matrix, d, b, f).setColor(color)
            vertexconsumer.addVertex(matrix, d, e, f).setColor(color)
            vertexconsumer.addVertex(matrix, a, e, f).setColor(color)
        }

        Direction.WEST -> {
            vertexconsumer.addVertex(matrix, a, b, c).setColor(color)
            vertexconsumer.addVertex(matrix, a, b, f).setColor(color)
            vertexconsumer.addVertex(matrix, a, e, f).setColor(color)
            vertexconsumer.addVertex(matrix, a, e, c).setColor(color)
        }

        Direction.EAST -> {
            vertexconsumer.addVertex(matrix, d, b, c).setColor(color)
            vertexconsumer.addVertex(matrix, d, e, c).setColor(color)
            vertexconsumer.addVertex(matrix, d, e, f).setColor(color)
            vertexconsumer.addVertex(matrix, d, b, f).setColor(color)
        }
    }
}

internal fun renderShape(
    poseStack: PoseStack,
    vertexConsumer: VertexConsumer,
    shape: VoxelShape,
    offsetX: Double,
    offsetY: Double,
    offsetZ: Double,
    color: Int,
    lineWidth: Float = 1f,
) {
    //? if < 1.21.11 {
    /*val prevLineWidth = RenderSystem.getShaderLineWidth()
    RenderSystem.lineWidth(lineWidth)
    *///?}
    ShapeRenderer.renderShape(
        poseStack,
        vertexConsumer,
        shape,
        offsetX, offsetY, offsetZ, color, /*? > 1.21.10 >>*/lineWidth,
    )
    //? if < 1.21.11 {
    /*RenderSystem.lineWidth(prevLineWidth)
    *///?}
}


object RenderUtils {

    val blockAABB = Shapes.create(AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))

    fun RenderWorldEvent.renderBox(pos: BlockPos, color: UInt = 0xFFFFFFFFu) {
        val color = color.toInt()
        renderShape(
            poseStack,
            buffer.getBuffer(MLibRenderTypes.BLOCK_FILL_TRIANGLE_THROUGH_WALLS),
            blockAABB,
            pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), color,
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

    private val SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back")
    private val SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.withDefaultNamespace("container/slot_highlight_front")

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
        val (a, b, c, d, e, f) = when (direction) {
            Direction.UP, Direction.DOWN -> Vec6f(startX, z, startY, endX, z, endY)
            Direction.NORTH, Direction.SOUTH -> Vec6f(startX, startY, z, endX, endY, z)
            Direction.EAST, Direction.WEST -> Vec6f(z, startX, startY, z, endX, endY)
        }

        val matrix = poseStack.last().pose()
        val vertexconsumer = buffer.getBuffer(MLibRenderTypes.BLOCK_FILL_QUAD)

        when (direction) {
            Direction.DOWN -> {
                vertexconsumer.addVertex(matrix, a, b, c).setColor(color)
                vertexconsumer.addVertex(matrix, d, b, c).setColor(color)
                vertexconsumer.addVertex(matrix, d, b, f).setColor(color)
                vertexconsumer.addVertex(matrix, a, b, f).setColor(color)
            }

            Direction.UP -> {
                vertexconsumer.addVertex(matrix, a, e, c).setColor(color)
                vertexconsumer.addVertex(matrix, a, e, f).setColor(color)
                vertexconsumer.addVertex(matrix, d, e, f).setColor(color)
                vertexconsumer.addVertex(matrix, d, e, c).setColor(color)
            }

            Direction.NORTH -> {
                vertexconsumer.addVertex(matrix, a, b, c).setColor(color)
                vertexconsumer.addVertex(matrix, a, e, c).setColor(color)
                vertexconsumer.addVertex(matrix, d, e, c).setColor(color)
                vertexconsumer.addVertex(matrix, d, b, c).setColor(color)
            }

            Direction.SOUTH -> {
                vertexconsumer.addVertex(matrix, a, b, f).setColor(color)
                vertexconsumer.addVertex(matrix, d, b, f).setColor(color)
                vertexconsumer.addVertex(matrix, d, e, f).setColor(color)
                vertexconsumer.addVertex(matrix, a, e, f).setColor(color)
            }

            Direction.WEST -> {
                vertexconsumer.addVertex(matrix, a, b, c).setColor(color)
                vertexconsumer.addVertex(matrix, a, b, f).setColor(color)
                vertexconsumer.addVertex(matrix, a, e, f).setColor(color)
                vertexconsumer.addVertex(matrix, a, e, c).setColor(color)
            }

            Direction.EAST -> {
                vertexconsumer.addVertex(matrix, d, b, c).setColor(color)
                vertexconsumer.addVertex(matrix, d, e, c).setColor(color)
                vertexconsumer.addVertex(matrix, d, e, f).setColor(color)
                vertexconsumer.addVertex(matrix, d, b, f).setColor(color)
            }
        }
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
            translate(x, y + 0.01f, z)
            val vc = buffer.getBuffer(MLibRenderTypes.BLOCK_FILL_QUAD)
            val pose = poseStack.last().pose()

            for (i in 0 until 360) {
                val rad = Math.toRadians(i.toDouble())
                val nextRad = Math.toRadians(i + 1.toDouble())

                val x1 = (radius * cos(rad)).toFloat()
                val z1 = (radius * sin(rad)).toFloat()
                val x2 = (radius * cos(nextRad)).toFloat()
                val z2 = (radius * sin(nextRad)).toFloat()

                // Inside
                vc.addVertex(pose, x1, 0f, z1).setColor(color)
                vc.addVertex(pose, x2, 0f, z2).setColor(color)
                vc.addVertex(pose, x2, height, z2).setColor(color)
                vc.addVertex(pose, x1, height, z1).setColor(color)

                // Outside
                vc.addVertex(pose, x2, 0f, z2).setColor(color)
                vc.addVertex(pose, x1, 0f, z1).setColor(color)
                vc.addVertex(pose, x1, height, z1).setColor(color)
                vc.addVertex(pose, x2, height, z2).setColor(color)
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
            translate(x, y + 0.01f, z)
            val vc = buffer.getBuffer(MLibRenderTypes.BLOCK_FILL_QUAD)
            val pose = poseStack.last().pose()

            for (i in 0 until 360) {
                val rad = Math.toRadians(i.toDouble())
                val nextRad = Math.toRadians((i + 1).toDouble())

                val x1 = (radius * cos(rad)).toFloat()
                val z1 = (radius * sin(rad)).toFloat()
                val x2 = (radius * cos(nextRad)).toFloat()
                val z2 = (radius * sin(nextRad)).toFloat()

                vc.addVertex(pose, 0f, 0f, 0f).setColor(color)
                vc.addVertex(pose, x2, 0f, z2).setColor(color)
                vc.addVertex(pose, x1, 0f, z1).setColor(color)
                vc.addVertex(pose, 0f, 0f, 0f).setColor(color)
            }
        }
    }
}
