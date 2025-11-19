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

            context is RiftInventoryContext && other is RiftInventoryContext -> {}

            context is RiftEnderchestPageContext && other is RiftEnderchestPageContext -> {
                if (context.index == other.index) return
                this.context = RiftStorageContext
            }

            context is AbstractRiftStorageContext && other is AbstractRiftStorageContext -> {}

            context is RiftItemContext && other is RiftItemContext -> {}

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
        val newContext = newItem.context
        when {
            newContext is ChestItemContext -> chests.add(newContext.chestPos)

            newContext is EquipmentItemContext && !any<EquipmentItemContext>() -> contexts.add(EquipmentItemContext)
            newContext is InventoryItemContext && !any<InventoryItemContext>() -> contexts.add(InventoryItemContext)

            newContext is AbstractStorageItemContext && any<StorageItemContext>() -> {} // skip

            newContext is AbstractStorageItemContext -> {
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
