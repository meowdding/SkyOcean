package me.owdding.skyocean.features.item.soures

import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.PlayerStorageInstance
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.StorageAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object StorageItemSource : ItemSource {
    override fun getAll(): List<SimpleTrackedItem> = buildList {
        addAll(StorageAPI.backpacks.convert(::BackpackStorageItemContext))
        addAll(StorageAPI.enderchests.convert(::EnderChestStorageItemContext))
    }

    private fun List<PlayerStorageInstance>.convert(function: (Int) -> ItemContext): List<SimpleTrackedItem> {
        return this.flatMap { (index, stacks) ->
            val context = function(index + 1)
            stacks.map { stack -> SimpleTrackedItem(stack, context) }
        }
    }

    override val type = ItemSources.STORAGE
}

interface AbstractStorageItemContext : ItemContext {
    override val source get() = ItemSources.STORAGE
}

object StorageItemContext : AbstractStorageItemContext {
    override fun collectLines() = build {
        add("Storage") { color = TextColor.GRAY }
        add("Click to open storage!") { this.color = TextColor.YELLOW }
    }

    override fun open() = McClient.sendCommand("storage")
}

data class BackpackStorageItemContext(
    val index: Int,
) : AbstractStorageItemContext {
    override fun collectLines() = build {
        add("Backpack Page $index") { color = TextColor.GRAY }
        add("Click to open backpack!") { this.color = TextColor.YELLOW }
    }

    override fun open() = McClient.sendCommand("/bp $index")
}

data class EnderChestStorageItemContext(
    val index: Int,
) : AbstractStorageItemContext {
    override fun collectLines() = build {
        add("Enderchest Page $index") { color = TextColor.GRAY }
        add("Click to open enderchest!") { this.color = TextColor.YELLOW }
    }

    override fun open() = McClient.sendCommand("/ec $index")
}
