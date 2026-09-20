package me.owdding.skyocean.utils.extensions

import me.owdding.lib.extensions.floor
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.sqrt

fun BlockPos.toVec3(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
fun BlockPos.toVec3Lower(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
fun BlockPos.toVec3LowerUpperY(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble() + 1, this.z.toDouble())

// Vec3s should be floored, not truncated when converting
fun Vec3.toBlockPos(): BlockPos = BlockPos(this.x.floor(), this.y.floor(), this.z.floor())

fun Vector3f.distance(other: Vec3) = distance(other.x.toFloat(), other.y.toFloat(), other.z.toFloat())
fun Vector3f.horizontalDistance(other: Vec3): Float {
    val dx = this.x - other.x.toFloat()
    val dz = this.z - other.z.toFloat()
    return sqrt(dx * dx + dz * dz)
}
fun Vector3f.verticalDistance(other: Vec3): Float = abs(this.y - other.y.toFloat())
fun Vector3f.toBlockPos() = BlockPos(x.floor(), y.floor(), z.floor())
