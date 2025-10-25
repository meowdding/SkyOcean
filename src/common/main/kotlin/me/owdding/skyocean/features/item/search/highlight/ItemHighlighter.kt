package me.owdding.skyocean.features.item.search.highlight

import com.teamresourceful.resourcefullib.common.color.Color
import kotlinx.coroutines.*
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.MiscConfig
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
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.InventoryTitle
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.MustBeContainer
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.PRIVATE_ISLAND
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.PlayerStorageInstance
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.StorageAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import java.util.concurrent.CopyOnWriteArrayList

@Module
object ItemHighlighter {

    private var currentSearch: ItemFilter? = null
    private val allItems = mutableSetOf<ItemStack>()
    private var future: Job? = null
    private val chests: MutableList<BlockPos> = CopyOnWriteArrayList()

    fun setHighlight(filter: ItemFilter?) = McClient.runNextTick {
        allItems.forEach { it.replaceVisually(null) }
        currentSearch = filter
        chests.clear()
        cancelOrScheduleClear()
    }

    fun addChests(chests: Collection<BlockPos>) = this.chests.addAll(chests)
    fun addChest(chest: BlockPos) = this.chests.add(chest)

    fun cancelOrScheduleClear() {
        future?.cancel(CancellationException("Item search has been canceled"))
        future = CoroutineScope(Dispatchers.Default).launch {
            delay(MiscConfig.highlightTime)
            resetSearch()
        }
        future?.start()
    }

    fun resetSearch() = setHighlight(null)

    private fun ItemStack.highlight() {
        this.skyoceanReplace(false) {
            if (this@highlight in ItemTag.GLASS_PANES) {
                item = MiscConfig.itemSearchItemHighlight.paneItem
            } else {
                backgroundItem = MiscConfig.itemSearchItemHighlight.paneStack
            }
        }
        allItems.add(this)
    }

    @Subscription
    @OptIn(ItemSearchComponent::class)
    fun onItem(event: ItemStackCreateEvent) {
        val filter = currentSearch ?: return
        if (filter.test(event.itemStack)) {
            event.itemStack.highlight()
        }
    }

    @Subscription
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
    fun recalculate() {
        val filter = currentSearch ?: return
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
    @OnlyIn(PRIVATE_ISLAND)
    private fun RenderWorldEvent.AfterTranslucent.renderWorld() {
        atCamera {
            chests.forEach { block ->
                renderBox(block, ARGB.color(125, Color.RAINBOW.value).toUInt())
            }
        }
    }
}
