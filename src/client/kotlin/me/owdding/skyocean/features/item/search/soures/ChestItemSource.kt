package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.data.profile.IslandChestStorage
import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import net.minecraft.core.BlockPos

object ChestItemSource : ItemSource {
    override fun getAll() = IslandChestStorage.getItems().map { (itemStack, _, pos, _) ->
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
