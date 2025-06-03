package me.owdding.skyocean.data.profile

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.CodecHelpers
import me.owdding.skyocean.utils.storage.ProfileStorage
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import java.util.concurrent.CopyOnWriteArrayList

object IslandChestStorage {

    private val storage = ProfileStorage(
        0,
        { CopyOnWriteArrayList() },
        "chests",
        {
            CodecHelpers.copyOnWriteList(SkyOceanCodecs.ChestItemCodec.codec())
        },
    )

    fun getItems(): List<ChestItem> {
        return storage.get() ?: mutableListOf()
    }

    fun removeBlock(position: BlockPos) {
        val list = storage.get() ?: return
        list.removeAll { (_, _, pos) -> pos == position }
        val filter = list.filter { (_, _, _, pos2) -> pos2 == position }
        list.removeAll(filter)
        list.addAll(filter.map { (itemStack, slot, pos) -> ChestItem(itemStack, slot, pos, null) })
    }

    fun addItem(item: ItemStack, slot: Int, pos1: BlockPos, pos2: BlockPos?) {
        this.storage.get()?.add(ChestItem(item, slot, pos1, pos2))
    }

    fun save() {
        this.storage.save()
    }
}

@GenerateCodec
data class ChestItem(
    @FieldName("item_stack") val itemStack: ItemStack,
    val slot: Int = 0,
    val pos: BlockPos,
    val pos2: BlockPos?,
)
