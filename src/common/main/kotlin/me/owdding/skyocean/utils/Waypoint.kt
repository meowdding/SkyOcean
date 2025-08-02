package me.owdding.skyocean.utils

import me.owdding.skyocean.utils.extensions.toVec3
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.awt.Color

data class Waypoint(
    val component: Component,
    val pos: Vec3,
    val scaleMarkiplier: Float = 1.0f,
    val distanceText: Boolean = true,
    val box: Boolean = false
) {

    constructor(component: Component, pos: BlockPos, scaleMarkiplier: Float = 1.0f, distanceText: Boolean = true) : this(component, pos.toVec3(), scaleMarkiplier, distanceText)

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
