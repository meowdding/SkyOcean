package me.owdding.skyocean.features.inventory

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.inventory.InventoryConfig
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.InventoryTitle
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.SlotClickEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.api.profile.items.museum.MuseumAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

@Module
object SalvagingHelper {

    private val red = 0xffd20f39.toInt()

    private val highlightedItems = mutableSetOf<Int>()
    private val salvagingUndonated: Boolean
        get() = highlightedItems.isNotEmpty()
    private const val SALVAGE_ITEMS_SLOT = 40

    @Subscription
    @InventoryTitle("Salvage Items")
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!InventoryConfig.salvagingHelper || event.isInPlayerInventory) return

        val slotsByIndex = event.inventory.associateBy { it.index }
        val salvageSlot = slotsByIndex[SALVAGE_ITEMS_SLOT] ?: return

        highlightedItems.removeAll { index ->
            slotsByIndex[index]?.item?.isEmpty != false
        }

        val itemStack = event.item
        val skyblockId = itemStack.getSkyBlockId()
        if (skyblockId != null && !MuseumAPI.isDonated(skyblockId)) {
            highlightedItems.add(event.slot.index)
        }

        if (!InventoryConfig.salvagingHelperHighlight) return

        highlightedItems.forEach { slotIndex ->
            slotsByIndex[slotIndex]?.item?.skyoceanReplace(false) {
                this.backgroundColor = red
            }
        }

        if (salvagingUndonated) {
            salvageSlot.item.skyoceanReplace {
                this.backgroundColor = red
            }
        }
    }

    @Subscription
    @InventoryTitle("Salvage Items")
    fun onInventoryClick(event: SlotClickEvent) {
        if (!InventoryConfig.salvagingHelper) return
        if (event.slot.index != SALVAGE_ITEMS_SLOT) return
        if (!salvagingUndonated) return

        if (InventoryConfig.salvagingHelperBlockSalvage) {
            event.cancel()
            McPlayer.self?.playSound(SoundEvents.ANVIL_LAND, 0.8f, 1f)
        }
    }

    @Subscription(ContainerCloseEvent::class)
    fun onInventoryClose() {
        if (!InventoryConfig.salvagingHelper) return
        McPlayer.inventory.forEach { itemStack -> itemStack.replaceVisually(null) }
        highlightedItems.clear()
    }

}
