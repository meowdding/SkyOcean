package me.owdding.skyocean.features.inventory

import com.mojang.serialization.Codec
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.codecs.CodecUtils

typealias InventoryData = MutableMap<InventoryType, MutableList<ItemStack>>

enum class InventoryType {
    NORMAL,
    RIFT,
    ;

    companion object {
        val CODEC: Codec<InventoryData> = CodecUtils.map(
            SkyOceanCodecs.getCodec<InventoryType>(),
            CodecUtils.list(
                ItemStack.OPTIONAL_CODEC,
            ),
        )
    }
}
