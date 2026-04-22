package me.owdding.skyocean.data.profile

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.extensions.asBlueprint
import me.owdding.skyocean.utils.items.ItemStackBlueprint
import me.owdding.skyocean.utils.levelBound
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

    fun hasBlock(position: BlockPos) = storage.get()?.any { (_, _, pos) -> pos == position } == true

    fun removeBlock(position: BlockPos) {
        val list = storage.get() ?: return
        list.removeAll { (_, _, pos) -> pos == position }
        val filter = list.filter { (_, _, _, pos2) -> pos2 == position }
        list.removeAll(filter.toSet())
        list.addAll(filter.map { (itemStack, slot, pos) -> ChestItem(itemStack, slot, pos, null) })
    }

    fun addItem(item: ItemStack, slot: Int, pos1: BlockPos, pos2: BlockPos?) {
        this.storage.get()?.add(ChestItem(item, slot, pos1, pos2))
    }

    fun clear() {
        this.storage.get()?.clear()
        save()
    }

    fun save() {
        this.storage.save()
    }
}

@GenerateCodec
data class ChestItem(
    @FieldName("item_stack") val itemStackBlueprint: ItemStackBlueprint,
    val slot: Int = 0,
    val pos: BlockPos,
    val pos2: BlockPos?,
) {
    constructor(template: ItemStack, slot: Int = 0, pos: BlockPos, pos2: BlockPos?) : this(template.asBlueprint(), slot, pos, pos2)

    operator fun component5() = itemStack

    val itemStack: ItemStack by levelBound { itemStackBlueprint.create() }
    val posList: List<BlockPos> get() = listOfNotNull(pos, pos2)
}

