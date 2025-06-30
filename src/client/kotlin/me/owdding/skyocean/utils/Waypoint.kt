package me.owdding.skyocean.utils

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.awt.Color
import kotlin.math.max

data class Waypoint(
    val component: Component,
    val pos: Vec3,
    val scaleMarkiplier: Float = 1.0f,
    val distanceText: Boolean = true,
    val box: Boolean = false
) {

    constructor(component: Component, pos: BlockPos, scaleMarkiplier: Float = 1.0f, distanceText: Boolean = true) : this(component, pos.toVec3(), scaleMarkiplier, distanceText)

    fun render(poseStack: PoseStack, camera: Camera, buffer: MultiBufferSource) {
        drawString(component, poseStack, camera, buffer)
        if (distanceText) {
            val dist = camera.position.distanceTo(pos).toInt()
            val distText = "${dist}m"
            drawString(Text.of(distText).withColor(Color.YELLOW.rgb), poseStack, camera, buffer, 10f) // negative makes it go up somehow?? end me
        }
    }

    private fun drawString(
        component: Component,
        poseStack: PoseStack,
        camera: Camera,
        buffer: MultiBufferSource,
        yOffset: Float = 0f
    ) {
        val font = McClient.self.font
        var scale = max((camera.position.distanceTo(pos).toFloat() / 10).toDouble(), 1.0).toFloat()
        scale *= 0.025f
        scale *= scaleMarkiplier

        poseStack.pushPop {
            translate(
                pos.x - camera.position.x + 0.5,
                pos.y - camera.position.y + 1,
                pos.z - camera.position.z + 0.5
            )
            poseStack.mulPose(camera.rotation())
            poseStack.scale(scale, -scale, scale)

            val xOffset = -font.width(component) / 2f

            font.drawInBatch(
                component,
                xOffset,
                yOffset,
                Color.WHITE.rgb,
                false,
                poseStack.last().pose(),
                buffer,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LightTexture.FULL_BRIGHT
            )
        }
    }

    companion object {

        val color: Color = Color(0, 255, 255, 100)

        fun create(): Waypoint {
            val pos = McClient.self.cameraEntity!!.blockPosition()
            return Waypoint(Text.of("${pos.x}, ${pos.y}, ${pos.z}"), pos)
        }

        fun create(text: String = "", pos: BlockPos = McClient.self.cameraEntity!!.blockPosition(), scaleMarkiplier: Float = 1.0f, distanceText: Boolean = true, box: Boolean = true): Waypoint {
            return Waypoint(Text.of(text), pos.toVec3(), scaleMarkiplier, distanceText, box)
        }
    }
}
