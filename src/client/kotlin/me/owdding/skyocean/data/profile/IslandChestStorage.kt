package me.owdding.skyocean.data.profile

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.storage.ProfileStorage
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack

object IslandChestStorage {

    private val storage = ProfileStorage(
        0,
        { mutableListOf<ChestItem>() },
        "chests",
        {
            CodecUtils.mutableList(SkyOceanCodecs.ChestItemCodec.codec())
        },
    )

    fun getItems(): List<ChestItem> {
        return storage.get() ?: mutableListOf()
    }

}

@GenerateCodec
data class ChestItem(
    @FieldName("item_stack") val itemStack: ItemStack,
    val slot: Int = 0,
    val pos: BlockPos,
    val pos2: BlockPos?,
)
