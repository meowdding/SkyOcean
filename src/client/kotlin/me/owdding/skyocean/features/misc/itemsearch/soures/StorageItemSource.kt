package me.owdding.skyocean.features.misc.itemsearch.soures

import me.owdding.skyocean.features.misc.itemsearch.ItemContext
import me.owdding.skyocean.features.misc.itemsearch.TrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.storage.PlayerStorageInstance
import tech.thatgravyboat.skyblockapi.api.profile.storage.StorageAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient

object StorageItemSource : ItemSource {
    override fun getAll(): List<TrackedItem> = buildList {
        addAll(StorageAPI.backpacks.convert(::BackpackStorageItemContext))
        addAll(StorageAPI.enderchests.convert(::EnderChestStorageItemContext))
    }

    private fun List<PlayerStorageInstance>.convert(function: (Int) -> ItemContext): List<TrackedItem> {
        return this.flatMap { (index, stacks) ->
            val context = function(index + 1)
            stacks.map { stack -> TrackedItem(stack, context) }
        }
    }

    override fun remove(item: TrackedItem) {
        TODO("Not yet implemented")
    }

    override val type = ItemSources.STORAGE
}

interface AbstractStorageItemContext : ItemContext

object StorageItemContext : AbstractStorageItemContext {
    override fun collectLines() = build {
        add("Storage")
    }

    override fun open() = McClient.sendCommand("storage")
}

data class BackpackStorageItemContext(
    val index: Int,
) : AbstractStorageItemContext {
    override fun collectLines() = build {
        add("Backpack: $index")
    }

    override fun open() = McClient.sendCommand("/bp $index")
}

data class EnderChestStorageItemContext(
    val index: Int,
) : AbstractStorageItemContext {
    override fun collectLines() = build {
        add("Enderchest: $index")
    }

    override fun open() = McClient.sendCommand("/ec $index")
}
