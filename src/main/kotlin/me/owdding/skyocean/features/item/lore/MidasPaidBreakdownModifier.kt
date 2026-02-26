package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@ItemModifier
object MidasPaidBreakdownModifier : AbstractItemModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.midas_bid_breakdown"
    override val isEnabled: Boolean get() = LoreModifierConfig.midasBidBreakdown


    override fun appliesTo(itemStack: ItemStack) = itemStack.getData(DataTypes.MIDAS_WEAPON_ADDED_COINS) != null

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {
        val originalBid = item.getData(DataTypes.MIDAS_WEAPON_BID) ?: return@withMerger null
        val addedCoins = item.getData(DataTypes.MIDAS_WEAPON_ADDED_COINS) ?: return@withMerger null

        addAfterNext({ it.stripped.contains("Price paid: ") }) {
            add {
                append("Original Bid: ", TextColor.DARK_GRAY)
                append(originalBid.toFormattedString(), TextColor.GOLD)
            }
            add {
                append("Added Coins: ", TextColor.DARK_GRAY)
                append(addedCoins.toFormattedString(), TextColor.GOLD)
            }
        }

        Result.modified
    }
}
