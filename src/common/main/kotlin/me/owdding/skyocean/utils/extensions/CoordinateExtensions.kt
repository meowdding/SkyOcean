package me.owdding.skyocean.utils.extensions

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d

fun BlockPos.toVec3(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
fun BlockPos.toVec3Lower(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
fun BlockPos.toVec3LowerUpperY(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble() + 1, this.z.toDouble())
fun Vec3.toBlockPos(): BlockPos = BlockPos(this.x.toInt(), this.y.toInt(), this.z.toInt())
fun Vec3.toVector3d(): Vector3d = Vector3d(this.x, this.y, this.z)
