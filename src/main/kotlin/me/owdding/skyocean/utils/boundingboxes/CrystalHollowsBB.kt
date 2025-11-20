package me.owdding.skyocean.utils.boundingboxes

import net.minecraft.core.Vec3i
import net.minecraft.world.level.levelgen.structure.BoundingBox

object CrystalHollowsBB {
    private fun create(pos1: Vec3i, pos2: Vec3i): BoundingBox = BoundingBox.fromCorners(pos1, pos2)

    const val HEAT_END = 64
    const val MAX_Y = 190

    const val MIN = 0
    const val MAX = 1024
    const val HALF = MAX / 2

    val ALL = create(Vec3i(MIN, MIN, MIN), Vec3i(MAX, MAX_Y, MAX))
    val MAGMA_FIELDS = create(Vec3i(MIN, MIN, MIN), Vec3i(MAX, HEAT_END - 1, MAX))
    val NUCLEUS = create(Vec3i(463, HEAT_END, 460), Vec3i(559, MAX_Y, 562))

    val MITHRIL = create(Vec3i(HALF + 1, HEAT_END, HALF), Vec3i(MAX, MAX_Y, MIN))
    val PRECURSOR = create(Vec3i(HALF, HEAT_END, HALF + 1), Vec3i(MAX, MAX_Y, MAX))
    val JUNGLE = create(Vec3i(HALF, HEAT_END, HALF), Vec3i(MIN, MAX_Y, MIN))
    val GOBLIN = create(Vec3i(HALF, HEAT_END, HALF + 1), Vec3i(MIN, MAX_Y, MAX))
}
