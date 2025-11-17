package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.PlayerStorageInstance
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.StorageAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color


internal fun List<PlayerStorageInstance>.convert(function: (Int) -> ItemContext): List<SimpleTrackedItem> {
    return this.flatMap { (index, stacks) ->
        val context = function(index + 1)
        stacks.map { stack -> SimpleTrackedItem(stack, context) }
    }
}

object StorageItemSource : ItemSource {
    override fun getAll(): List<SimpleTrackedItem> = buildList {
        addAll(StorageAPI.backpacks.convert(::BackpackStorageItemContext))
        addAll(StorageAPI.enderchests.convert(::EnderChestStorageItemContext))
    }

    override val type = ItemSources.STORAGE
}

interface AbstractStorageItemContext : ItemContext {
    override val source get() = ItemSources.STORAGE
}

object StorageItemContext : AbstractStorageItemContext {
    override fun collectLines() = build {
        add("Storage") { color = TextColor.GRAY }
        requiresOverworld { add("Click to open storage!") { this.color = TextColor.YELLOW } }
        riftWarning()
    }

    override fun open() = requiresOverworld(true) { McClient.sendCommand("storage") }
}

data class BackpackStorageItemContext(
    val index: Int,
) : AbstractStorageItemContext {
    override fun collectLines() = build {
        add("Backpack Page $index") { color = TextColor.GRAY }
        requiresOverworld { add("Click to open backpack!") { this.color = TextColor.YELLOW } }
        riftWarning()
    }

    override fun open() = requiresOverworld(true) { McClient.sendCommand("/bp $index") }
}

data class EnderChestStorageItemContext(
    val index: Int,
) : AbstractStorageItemContext {
    override fun collectLines() = build {
        add("Enderchest Page $index") { color = TextColor.GRAY }
        requiresOverworld { add("Click to open enderchest!") { this.color = TextColor.YELLOW } }
        riftWarning()
    }

    override fun open() = requiresOverworld(true) { McClient.sendCommand("/ec $index") }
}
