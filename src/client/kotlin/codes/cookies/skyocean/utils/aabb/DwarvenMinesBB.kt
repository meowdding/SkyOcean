package codes.cookies.skyocean.utils.aabb

import net.minecraft.core.Vec3i
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

object DwarvenMinesBB {
    private fun create(pos1: Vec3i, pos2: Vec3i) = SkyblockBB(SkyBlockIsland.DWARVEN_MINES, pos1, pos2)

    val GLACITE_TUNNELS = create(Vec3i(-128, 174, 184), Vec3i(127, 112, 479))
    val MIST = create(Vec3i(-78, 64, 28), Vec3i(182, 131, 162))

}
