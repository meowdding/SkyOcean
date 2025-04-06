package codes.cookies.skyocean.utils.aabb

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.levelgen.structure.BoundingBox
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

class SkyblockBB(private val island: SkyBlockIsland, pos1: Vec3i, pos2: Vec3i) : BoundingBox(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z) {
    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        return island.inIsland() && super.isInside(x, y, z)
    }

    operator fun contains(pos: BlockPos): Boolean {
        return isInside(pos)
    }
}
