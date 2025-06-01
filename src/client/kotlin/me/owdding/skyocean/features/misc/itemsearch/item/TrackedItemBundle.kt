package me.owdding.skyocean.features.misc.itemsearch.item

import me.owdding.skyocean.features.misc.itemsearch.ItemContext
import me.owdding.skyocean.features.misc.itemsearch.soures.*
import net.minecraft.network.chat.CommonComponents
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append

class TrackedItemBundle(trackedItem: TrackedItem) : TrackedItem {
    override val itemStack: ItemStack = trackedItem.itemStack.copy()
    override var context: ItemContext = trackedItem.context
        private set

    val items: MutableList<TrackedItem> = mutableListOf(trackedItem)

    override fun add(other: TrackedItem): TrackedItem {
        this.items.add(other)
        this.itemStack.count += other.itemStack.count
        this.updateContext(other)
        return this
    }

    private fun updateContext(newItem: TrackedItem) {
        val context = context
        val other = newItem.context
        when {
            context is BundledItemContext -> context.add(newItem)
            context is EnderChestStorageItemContext && other is EnderChestStorageItemContext -> {
                if (context.index == other.index) return
                this.context = StorageItemContext
            }

            context is BackpackStorageItemContext && other is BackpackStorageItemContext -> {
                if (context.index == other.index) return
                this.context = StorageItemContext
            }

            context is AbstractStorageItemContext && other is AbstractStorageItemContext -> {
                this.context = StorageItemContext
            }

            context is InventoryItemContext && other is InventoryItemContext -> {}
            context is EquipmentItemContext && other is EquipmentItemContext -> {}
            context !is BundledItemContext -> {
                this.context = BundledItemContext().apply {
                    this@TrackedItemBundle.items.forEach(::add)
                }
            }

            else -> {
                context.add(newItem)
            }
        }
    }
}

data class BundledItemContext(val map: MutableMap<ItemSources, Int> = mutableMapOf()) : ItemContext {
    override fun collectLines() = build {
        map.entries.sortedBy { it.key.ordinal }.forEach { (key, value) ->
            add(key.name.toTitleCase()) {
                append(":")
                append(CommonComponents.SPACE)
                append(value)
            }
        }
    }

    fun add(newItem: TrackedItem) {
        assert(newItem.context.source != ItemSources.BUNDLE)
        map.merge(newItem.context.source, newItem.itemStack.count, Int::plus)
    }

    override val source = ItemSources.BUNDLE
}
