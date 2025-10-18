package me.owdding.skyocean.utils.boundingboxes

import net.minecraft.core.Vec3i
import net.minecraft.world.level.levelgen.structure.BoundingBox

object DwarvenMinesBB {
    private fun create(pos1: Vec3i, pos2: Vec3i): BoundingBox = BoundingBox.fromCorners(pos1, pos2)

    val GLACITE_TUNNELS = create(Vec3i(-128, 112, 184), Vec3i(127, 174, 479))

    val MIST = Octree(
        create(Vec3i(-73, 88, 162), Vec3i(181, 64, 34)),
        create(Vec3i(175, 89, 99), Vec3i(106, 117, 28)),
        create(Vec3i(105, 89, 98), Vec3i(68, 92, 66)),
        create(Vec3i(76, 89, 99), Vec3i(65, 94, 107)),
        create(Vec3i(-73, 89, 43), Vec3i(63, 106, 161)),
        create(Vec3i(-16, 107, 84), Vec3i(-35, 114, 72)),
        create(Vec3i(50, 107, 92), Vec3i(66, 112, 124)),
        create(Vec3i(61, 122, 122), Vec3i(43, 113, 136)),
        create(Vec3i(34, 107, 103), Vec3i(20, 131, 116)),
        create(Vec3i(-34, 123, 114), Vec3i(-17, 107, 105)),
    )
    val GEMSTONE_LOCATIONS = Octree(
        create(Vec3i(90, 218, -108), Vec3i(93, 224, -86)),
        create(Vec3i(79, 197, -127), Vec3i(94, 203, -119)),
        create(Vec3i(-128, 112, 243), Vec3i(127, 174, 479)),
        create(Vec3i(-51, 119, 239), Vec3i(-49, 125, 242)),
        create(Vec3i(-23, 147, 236), Vec3i(-18, 153, 239)),
        create(Vec3i(-92, 142, 242), Vec3i(-127, 153, 225)),
    )

}
