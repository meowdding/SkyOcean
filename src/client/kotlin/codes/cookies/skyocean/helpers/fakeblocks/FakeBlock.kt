package codes.cookies.skyocean.helpers.fakeblocks

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block

data class FakeBlock(
    val original: Block,
    val id: ResourceLocation,
)
