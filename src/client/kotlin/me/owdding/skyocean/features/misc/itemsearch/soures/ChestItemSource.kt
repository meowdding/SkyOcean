package me.owdding.skyocean.features.misc.itemsearch.soures

import me.owdding.skyocean.data.profile.IslandChestData
import me.owdding.skyocean.features.misc.itemsearch.ItemContext
import me.owdding.skyocean.features.misc.itemsearch.item.SimpleTrackedItem
import net.minecraft.core.BlockPos

object ChestItemSource : ItemSource {
    override fun getAll() = IslandChestData.getItems().map { (itemStack, _, pos, _) ->
        SimpleTrackedItem(itemStack, ChestItemContext(pos))
    }

    override fun remove(item: SimpleTrackedItem) {

    }

    override val type = ItemSources.CHEST
}

data class ChestItemContext(
    val chestPos: BlockPos,
) : ItemContext {
    override val source = ItemSources.CHEST
    override fun collectLines() = build {
        add {
            append("Position: ")
            append(chestPos.toString())
        }
    }
}
