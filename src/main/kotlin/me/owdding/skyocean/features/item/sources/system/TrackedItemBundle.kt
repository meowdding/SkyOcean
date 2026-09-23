package me.owdding.skyocean.features.item.sources.system

import me.owdding.skyocean.features.item.search.highlight.ItemHighlighter
import me.owdding.skyocean.features.item.sources.*
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.CommonComponents
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class TrackedItemBundle(trackedItem: TrackedItem) : TrackedItem {
    override val itemStack: ItemStack = trackedItem.itemStack.copy()
    override var context: ItemContext = trackedItem.context
        private set
    override var price: Long = trackedItem.price
        private set

    val items: MutableList<TrackedItem> = mutableListOf(trackedItem)

    override fun add(other: TrackedItem): TrackedItem {
        this.items.add(other)
        this.price += other.price
        this.itemStack.count += other.itemStack.count
        this.updateContext(other)
        return this
    }

    private fun updateContext(newItem: TrackedItem) {
        val context = context
        val other = (newItem.context as? ParentItemContext)?.parent ?: newItem.context
        when (context) {
            is BundledItemContext -> context.add(newItem)
            is EnderChestStorageItemContext if other is EnderChestStorageItemContext -> {
                if (context.index == other.index) return
                this.context = StorageItemContext
            }

            is BackpackStorageItemContext if other is BackpackStorageItemContext -> {
                if (context.index == other.index) return
                this.context = StorageItemContext
            }

            is AbstractStorageItemContext if other is AbstractStorageItemContext -> {
                this.context = StorageItemContext
            }

            is RiftInventoryContext if other is RiftInventoryContext -> {}
            is RiftEnderchestPageContext if other is RiftEnderchestPageContext -> {
                if (context.index == other.index) return
                this.context = RiftStorageContext
            }

            is AbstractRiftStorageContext if other is AbstractRiftStorageContext -> {}
            is RiftItemContext if other is RiftItemContext -> {}
            is InventoryItemContext if other is InventoryItemContext -> {}
            is EquipmentItemContext if other is EquipmentItemContext -> {}
            !is BundledItemContext -> {
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
    val contexts = mutableListOf<ItemContext>()
    val chests = mutableSetOf<BlockPos>()
    lateinit var item: ItemStack

    override fun collectLines() = build {
        map.entries.sortedByDescending { it.key.ordinal }.forEach { (key, value) ->
            add(key.name.toTitleCase()) {
                append(":")
                append(CommonComponents.SPACE)
                append(value.toFormattedString())
                this.color = TextColor.GRAY
            }
        }

        if (map.contains(ItemSources.RIFT)) {
            requiresOverworld { add("Not currently in the rift!") { color = TextColor.RED } }
        } else {
            riftWarning()
        }
    }

    private inline fun <reified T> any() = contexts.filterIsInstance<T>().isNotEmpty()

    fun add(newItem: TrackedItem) {
        assert(newItem.context.source != ItemSources.BUNDLE)
        if (!this::item.isInitialized) {
            this.item = newItem.itemStack
        }
        map.merge(newItem.context.source, newItem.itemStack.count, Int::plus)
        when (val newContext = newItem.context) {
            is ChestItemContext -> chests.add(newContext.chestPos)
            is EquipmentItemContext if !any<EquipmentItemContext>() -> contexts.add(EquipmentItemContext)
            is InventoryItemContext if !any<InventoryItemContext>() -> contexts.add(InventoryItemContext)
            is AbstractStorageItemContext if any<StorageItemContext>() -> {} // skip
            is AbstractStorageItemContext -> {
                val other = contexts.filterIsInstance<AbstractStorageItemContext>().firstOrNull() // there should only ever be one entry per type
                val mergedContext: ItemContext = when {
                    other == null -> newContext

                    newContext is BackpackStorageItemContext && other is BackpackStorageItemContext ->
                        if (newContext.index == other.index) other else StorageItemContext

                    newContext is EnderChestStorageItemContext && other is EnderChestStorageItemContext ->
                        if (newContext.index == other.index) other else StorageItemContext

                    else -> StorageItemContext
                }
                other?.let(contexts::remove)
                contexts.add(mergedContext)
            }
        }
    }

    override val source = ItemSources.BUNDLE

    override fun open() = McClient.runNextTick {
        val context = map.entries.sortedByDescending { (_, value) -> value }
            .firstNotNullOfOrNull { (key, _) -> contexts.filterNot { it is OnPlayerItemContext }.firstOrNull { it.source == key } }

        context?.open()

        ItemHighlighter.addChests(chests)
    }
}
