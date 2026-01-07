package me.owdding.skyocean.features.item.search.highlight

import com.google.common.collect.Queues
import com.teamresourceful.resourcefullib.common.color.Color
import kotlinx.coroutines.*
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.data.profile.ChestItem
import me.owdding.skyocean.data.profile.IslandChestStorage
import me.owdding.skyocean.events.ItemSearchComponent
import me.owdding.skyocean.events.ItemStackCreateEvent
import me.owdding.skyocean.features.item.search.search.ItemFilter
import me.owdding.skyocean.repo.SackData
import me.owdding.skyocean.repo.SackData.sackRegex
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import me.owdding.skyocean.utils.rendering.RenderUtils.renderBox
import net.minecraft.core.BlockPos
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.*
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.PlayerStorageInstance
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.StorageAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.clearAnd
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import java.util.*
import java.util.Queue
import java.util.concurrent.atomic.AtomicBoolean


@Module
object ItemHighlighter {

    var currentSearch: ItemFilter? = null
        private set
    private val allItems: MutableSet<ItemStack> = Collections.newSetFromMap(WeakHashMap())
    private var future: Job? = null
    private val chests: MutableSet<BlockPos> = mutableSetOf()

    private var hasHighlightInCurrentInventory = false

    private val queue: Queue<ItemStack> = Queues.newConcurrentLinkedQueue()
    private var scheduled = AtomicBoolean(false)

    private fun scheduleAdd(item: ItemStack) {
        queue.add(item)
        if (!scheduled.compareAndSet(false, true)) return
        allItems.addAll(queue)
        McClient.self.executeIfPossible {
            while (true) {
                val item = queue.poll() ?: break
                allItems.add(item)
            }
            scheduled.set(false)
        }
    }

    fun setHighlight(
        filter: ItemFilter?,
        updateChests: Boolean = true,
        scheduleClear: Boolean = true,
    ) = McClient.runNextTick {
        allItems.clearAnd { it.replaceVisually(null) }
        currentSearch = filter
        chests.clear()
        if (filter == null) return@runNextTick
        if (scheduleClear) cancelOrScheduleClear()
        if (updateChests) recalculateChests()
        recalculate(filter)
    }

    fun recalculateChests() = McClient.self.executeIfPossible {
        chests.clear()
        val filter = currentSearch ?: return@executeIfPossible
        IslandChestStorage.getItems().filter { filter.test(it.itemStack) }
            .flatMapTo(mutableSetOf(), ChestItem::posList)
            .let(chests::addAll)
    }

    fun addChests(chests: Collection<BlockPos>) = McClient.self.executeIfPossible {
        this.chests.addAll(chests)
    }
    fun addChest(chest: BlockPos) = McClient.self.executeIfPossible {
        this.chests.add(chest)
    }

    fun cancelOrScheduleClear() {
        future?.cancel()
        future = CoroutineScope(Dispatchers.Default).launch {
            delay(MiscConfig.highlightTime)
            resetSearch()
        }.apply(Job::start)
    }

    fun resetSearch() = McClient.self.executeIfPossible {
        allItems.clearAnd { it.replaceVisually(null) }
        currentSearch = null
        chests.clear()
        future?.cancel()
        future = null
    }

    private fun ItemStack.highlight() {
        this.skyoceanReplace(false) {
            when (MiscConfig.itemSearchHighlightMode) {
                GLASS_PANE -> {
                    if (this@highlight in ItemTag.GLASS_PANES) {
                        item = MiscConfig.itemSearchItemHighlight.paneItem
                    } else {
                        backgroundItem = MiscConfig.itemSearchItemHighlight.paneStack
                    }
                }

                FILL -> {
                    backgroundColor = MiscConfig.itemSearchItemHighlight.color
                }
            }
        }
        scheduleAdd(this)
    }

    @Subscription
    @OnlyOnSkyBlock
    @OptIn(ItemSearchComponent::class)
    fun onItem(event: ItemStackCreateEvent) {
        val filter = currentSearch ?: return
        if (filter.test(event.itemStack)) {
            event.itemStack.highlight()
        }
    }

    /** Low priority so that [me.owdding.skyocean.features.misc.ChestTracker.onClose] gets called first */
    @OnlyOnSkyBlock
    @Subscription(ContainerCloseEvent::class, priority = Subscription.LOW)
    fun onContainerClose() {
        if (!hasHighlightInCurrentInventory) return
        hasHighlightInCurrentInventory = false
        if (SkyBlockIsland.PRIVATE_ISLAND.inIsland() && !LocationAPI.isGuest) recalculateChests()
    }

    @Subscription
    @OnlyNonGuest
    @MustBeContainer
    @OnlyIn(PRIVATE_ISLAND)
    fun onInventoryChange(event: InventoryChangeEvent) {
        val filter = currentSearch ?: return
        if (filter.test(event.item)) {
            hasHighlightInCurrentInventory = true
            event.item.highlight()
        }
    }

    @Subscription
    @OnlyOnSkyBlock
    @MustBeContainer
    @InventoryTitle("Sack of Sacks")
    fun onSackScreen(event: InventoryChangeEvent) {
        val filter = currentSearch ?: return
        if (event.isInBottomRow) return
        if (event.isSkyBlockFiller) return

        val id = event.item.getSkyBlockId()?.replace(sackRegex, "") ?: return
        val sack = SackData.getByNormalizedId(id) ?: return

        if (sack.containingItems.any { filter.test(it) }) {
            event.item.highlight()
        }
    }

    // TODO: add support for Sack/Storage highlighting when recalculating
    fun recalculate(filter: ItemFilter) {
        McPlayer.inventory.forEach {
            if (filter.test(it)) it.highlight()
        }
        val menu = McScreen.asMenu ?: return
        val slots = menu.menu.slots
        for (slot in slots) {
            val item = slot.item
            if (filter.test(item)) {
                item.highlight()
                continue
            }
        }
    }

    private val enderchest = Regex("Ender Chest Page (\\d)")
    private val backpack = Regex("Backpack Slot (\\d+)")

    @Subscription
    @OnlyOnSkyBlock
    @MustBeContainer
    @InventoryTitle("Storage")
    fun onStorage(event: InventoryChangeEvent) {
        val filter = currentSearch ?: return
        if (event.isInBottomRow) return
        val item = event.item

        val cleanName = item.cleanName
        val items: List<PlayerStorageInstance> = if (cleanName.matches(enderchest)) {
            StorageAPI.enderchests
        } else if (cleanName.matches(backpack)) {
            StorageAPI.backpacks
        } else {
            return
        }
        val id: Int = cleanName.filter { it.isDigit() }.toInt()

        items.find { storage -> storage.index == (id - 1) }?.let { handleStorage(it, item, filter) }
    }

    fun handleStorage(items: PlayerStorageInstance, item: ItemStack, filter: ItemFilter) {
        if (items.items.any { filter.test(it) }) {
            item.highlight()
        }
    }

    @Subscription
    @OnlyNonGuest
    @OnlyIn(PRIVATE_ISLAND)
    private fun RenderWorldEvent.AfterTranslucent.renderWorld() {
        atCamera {
            chests.forEach { block ->
                renderBox(block, ARGB.color(125, Color.RAINBOW.value).toUInt())
            }
        }
    }
}
