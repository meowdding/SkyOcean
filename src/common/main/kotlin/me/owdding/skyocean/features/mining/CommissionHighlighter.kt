package me.owdding.skyocean.features.mining

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore

@Module
object CommissionHighlighter {

    @Subscription
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!MiningConfig.modifyCommissions) return
        if (event.title != "Commissions") return
        if (event.isInPlayerInventory) return
        if (event.item !in Items.WRITABLE_BOOK) return

        val stripped = event.item.getRawLore().last().trim()
        event.item.skyoceanReplace {
            item = when (stripped) {
                "Click to claim rewards!" -> Items.KNOWLEDGE_BOOK
                "0%" -> Items.WRITTEN_BOOK
                else -> Items.WRITABLE_BOOK
            }
            set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false)
        }
    }

}
