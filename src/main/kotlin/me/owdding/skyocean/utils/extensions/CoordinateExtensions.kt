package me.owdding.skyocean.utils.extensions

import me.owdding.lib.extensions.floor
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

fun BlockPos.toVec3(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
fun BlockPos.toVec3Lower(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
fun BlockPos.toVec3LowerUpperY(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble() + 1, this.z.toDouble())

// Vec3s should be floored, not truncated when converting
fun Vec3.toBlockPos(): BlockPos = BlockPos(this.x.floor(), this.y.floor(), this.z.floor())
