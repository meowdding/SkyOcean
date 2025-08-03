package me.owdding.skyocean.features.inventory

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.CodecHelpers
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.codecs.CodecUtils

typealias InventoryData = MutableMap<InventoryType, MutableList<Pair<Int, ItemStack>>>

enum class InventoryType {
    NORMAL,
    RIFT,
    ;

    companion object {
        @IncludedCodec
        val CODEC: Codec<InventoryData> = CodecUtils.map(
            SkyOceanCodecs.getCodec<InventoryType>(),
            CodecUtils.list(
                CodecHelpers.pair(
                    Codec.INT,
                    ItemStack.OPTIONAL_CODEC,
                ),
            ),
        )
    }
}
