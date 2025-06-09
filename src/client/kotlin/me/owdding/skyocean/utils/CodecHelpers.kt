package me.owdding.skyocean.utils

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.IncludedCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import java.util.concurrent.CopyOnWriteArrayList

object CodecHelpers {

    fun <T> copyOnWriteList(original: Codec<T>): Codec<CopyOnWriteArrayList<T>> = original.listOf().xmap({ CopyOnWriteArrayList(it) }, { it })

    @IncludedCodec
    val ITEM_STACK_CODEC: Codec<ItemStack> = ItemStack.OPTIONAL_CODEC

    @IncludedCodec
    val BLOCK_POS_CODEC: Codec<BlockPos> = BlockPos.CODEC

}
