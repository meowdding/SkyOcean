package me.owdding.skyocean.features.garden

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.garden.GardenConfig
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.InventoryTitle
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.extentions.toIntValue
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.anyMatch

@Module
object DeskPestHighlight {
    private val regex = "ൠ This plot has (?<amount>.*) ൠ Pests?!".toRegex()

    @Subscription
    @InventoryTitle("Configure Plots")
    @OnlyIn(SkyBlockIsland.GARDEN)
    fun onInv(event: InventoryChangeEvent) {
        if (!GardenConfig.deskPestHighlight) return
        regex.anyMatch(event.item.getRawLore(), "amount") { (amount) ->
            val amount = amount.toIntValue().takeUnless { it == 0 } ?: return@anyMatch
            event.item.skyoceanReplace {
                backgroundItem = Items.RED_STAINED_GLASS_PANE.defaultInstance
                customSlotText = "§6$amount"
            }
        }
    }
}
