package codes.cookies.skyocean.utils.boundingboxes

import net.minecraft.core.Vec3i
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

object DwarvenMinesBB {
    private fun create(pos1: Vec3i, pos2: Vec3i) = SkyblockBB(SkyBlockIsland.DWARVEN_MINES, pos1, pos2)

    val GLACITE_TUNNELS = create(Vec3i(-128, 112, 184), Vec3i(127, 174, 479))
    val MIST = create(Vec3i(-78, 64, 28), Vec3i(182, 131, 162))
    val GEMSTONE_LOCATIONS = Octree(
        create(Vec3i(90, 218, -108), Vec3i(93, 224, -86)),
        create(Vec3i(79, 197, -127), Vec3i(94, 203, -119)),
        create(Vec3i(-128, 112, 243), Vec3i(127, 174, 479)),
        create(Vec3i(-51, 119, 239), Vec3i(-49, 125, 242)),
        create(Vec3i(-23, 147, 236), Vec3i(-18, 153, 239))
    )

}
