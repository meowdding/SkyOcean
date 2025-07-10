package me.owdding.skyocean.features.item.soures

import me.owdding.skyocean.data.profile.IslandChestStorage
import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.highlight.ItemHighlighter
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import net.minecraft.core.BlockPos
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object ChestItemSource : ItemSource {
    override fun getAll() = IslandChestStorage.getItems().map { (itemStack, _, pos, secondPos) ->
        SimpleTrackedItem(itemStack, ChestItemContext(pos, secondPos))
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
            append("Chest at x: ${chestPos.x}, y: ${chestPos.y}, z: ${chestPos.z}")
            color = TextColor.GRAY
        }
        add("Click to highlight chest!") { this.color = TextColor.YELLOW }
    }

    override fun open() = McClient.runNextTick {
        ItemHighlighter.addChest(chestPos)
        secondPos?.let(ItemHighlighter::addChest)
    }
}
