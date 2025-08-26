package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.data.profile.IslandChestStorage
import me.owdding.skyocean.features.item.search.highlight.ItemHighlighter
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.minecraft.core.BlockPos
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
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
        val clickText = if (SkyBlockIsland.PRIVATE_ISLAND.inIsland()) "Click to highlight chest!"
        else {
            if (MiscConfig.itemSearchWarpToIsland) "Click to warp to island and highlight chest!"
            else "Go to your island to highlight!"
        }
        requiresOverworld { add(clickText) { this.color = TextColor.YELLOW } }
        riftWarning()
    }

    override fun open() = requiresOverworld(true) {
        McClient.runNextTick {
            ItemHighlighter.addChest(chestPos)
            secondPos?.let(ItemHighlighter::addChest)

            if (!SkyBlockIsland.PRIVATE_ISLAND.inIsland() && MiscConfig.itemSearchWarpToIsland) {
                McClient.sendCommand("warp island")
            }
        }
    }
}
