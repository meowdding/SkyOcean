package me.owdding.skyocean.features.misc.itemsearch.soures

import me.owdding.skyocean.data.profile.IslandChestData
import me.owdding.skyocean.features.misc.itemsearch.ItemContext
import me.owdding.skyocean.features.misc.itemsearch.TrackedItem
import net.minecraft.core.BlockPos

object ChestItemSource : ItemSource {
    override fun getAll() = IslandChestData.getItems().map { (itemStack, _, pos, _) ->
        TrackedItem(itemStack, ChestItemContext(pos))
    }

    override fun remove(item: TrackedItem) {

    }

    override val type = ItemSources.CHEST
}

data class ChestItemContext(
    val chestPos: BlockPos,
) : ItemContext {
    override fun collectLines() = build {
        add {
            append("Position: ")
            append(chestPos.toString())
        }
    }
}
