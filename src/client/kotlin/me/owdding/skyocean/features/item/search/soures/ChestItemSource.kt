package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.data.profile.IslandChestStorage
import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.highlight.ItemHighlighter
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import net.minecraft.core.BlockPos
import tech.thatgravyboat.skyblockapi.helpers.McClient

object ChestItemSource : ItemSource {
    override fun getAll() = IslandChestStorage.getItems().map { (itemStack, _, pos, secondPos) ->
        SimpleTrackedItem(itemStack, ChestItemContext(pos, secondPos))
    }

    override fun remove(item: SimpleTrackedItem) {

    }

    override val type = ItemSources.CHEST
}

data class ChestItemContext(
    val chestPos: BlockPos,
    private val secondPos: BlockPos?,
) : ItemContext {
    override val source = ItemSources.CHEST
    override fun collectLines() = build {
        add {
            append("Position: ")
            append(chestPos.toString())
        }
    }

    override fun open() = McClient.tell {
        ItemHighlighter.addChest(chestPos)
        secondPos?.let(ItemHighlighter::addChest)
    }
}
