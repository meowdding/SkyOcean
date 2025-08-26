package me.owdding.skyocean.utils

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.ItemStack
import java.util.concurrent.CopyOnWriteArrayList

val PACK_FORMAT = SkyOceanCodecs.PackMetadataCodec.codec()

object CodecIsolation {
    @IncludedCodec
    val JSON_CODEC: Codec<JsonElement> = ExtraCodecs.JSON
}

object CodecHelpers {

    fun <T> copyOnWriteList(original: Codec<T>): Codec<CopyOnWriteArrayList<T>> = original.listOf().xmap({ CopyOnWriteArrayList(it) }, { it })

    @IncludedCodec
    val ITEM_STACK_CODEC: Codec<ItemStack> = ItemStack.OPTIONAL_CODEC

    @IncludedCodec
    val BLOCK_POS_CODEC: Codec<BlockPos> = BlockPos.CODEC

    @IncludedCodec
    val RESOURCE_LOCATION: Codec<ResourceLocation> = ResourceLocation.CODEC

    fun <T, B> pair(t: Codec<T>, b: Codec<B>): Codec<Pair<T, B>> = RecordCodecBuilder.create {
        it.group(
            t.fieldOf("first").forGetter { it.first },
            b.fieldOf("second").forGetter { it.second },
        ).apply(it, { a, b -> a to b })
    }
}
