package codes.cookies.skyocean.utils.tags

import codes.cookies.skyocean.SkyOcean
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import tech.thatgravyboat.skyblockapi.impl.tagkey.BlockTagKey

enum class BlockTagKey(path: String) : BlockTagKey {
    DWARVEN_MINES_CARPETS("dwarven_mines_carpets"),
    ;

    override val key: TagKey<Block> = TagKey.create(Registries.BLOCK, SkyOcean.id(path))
}
