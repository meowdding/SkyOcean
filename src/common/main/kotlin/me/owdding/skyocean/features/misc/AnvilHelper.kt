package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

@Module
object AnvilHelper {

    private val slots = intArrayOf(29, 33)

    @Subscription
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!MiscConfig.anvilHelper) return
        if (event.title != "Anvil") return
        if (!event.isInPlayerInventory) return
        val itemStacks = event.inventory
            .filter { it.index in slots }
            .filter { it.item in Items.ENCHANTED_BOOK }
            .mapNotNull { it.item }
            .takeUnless { it.isEmpty() } ?: return
        if (event.item !in Items.ENCHANTED_BOOK) return
        val enchants = itemStacks.map { it.getData(DataTypes.ENCHANTMENTS) }
            .mapNotNull { it?.entries?.map { (key, value) -> key to value } }.flatten().distinct().takeIf { it.size == 1 }?.first() ?: return
        val itemEnchants = event.item.getData(DataTypes.ENCHANTMENTS) ?: return
        if (itemEnchants[enchants.first] != enchants.second) return
        event.item.skyoceanReplace {
            this.item = Items.KNOWLEDGE_BOOK
        }
    }

    @Subscription(ContainerCloseEvent::class)
    fun onInventoryClose() {
        if (!MiscConfig.anvilHelper) return
        McPlayer.inventory.forEach { itemStack -> itemStack.replaceVisually(null) }
    }
}
