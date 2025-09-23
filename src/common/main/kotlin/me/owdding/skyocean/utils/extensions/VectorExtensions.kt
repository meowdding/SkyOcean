package me.owdding.skyocean.utils.extensions

import me.owdding.skyocean.utils.codecs.NamedPosition
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

fun BlockPos.toVec3(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
fun BlockPos.toVec3Lower(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
fun BlockPos.toVec3LowerUpperY(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble() + 1, this.z.toDouble())

fun Vec3.toBlockPos(): BlockPos = BlockPos(this.x.toInt(), this.y.toInt(), this.z.toInt())
fun Vec3?.orZero(): Vec3 = this ?: Vec3.ZERO

fun NamedPosition.toBlockPos() = BlockPos(x.toInt(), y.toInt(), z.toInt())
fun NamedPosition.toVec3() = Vec3(x, y, z)
fun BlockPos.toNamedPosition() = NamedPosition(x.toDouble(), y.toDouble(), z.toDouble())
fun Vec3.toNamedPosition() = NamedPosition(x, y, z)
