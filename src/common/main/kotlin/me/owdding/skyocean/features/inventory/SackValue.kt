package me.owdding.skyocean.features.inventory

import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.shorten
import me.owdding.skyocean.config.features.inventory.SackValueConfig
import me.owdding.skyocean.helpers.InventorySideGui
import me.owdding.skyocean.utils.Utils.unaryMinus
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.OceanColors.BETTER_GOLD
import me.owdding.skyocean.utils.extensions.asScrollable
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerInitializedEvent
import tech.thatgravyboat.skyblockapi.api.profile.items.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.ItemData
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.Pricing
import tech.thatgravyboat.skyblockapi.utils.extentions.containerHeight
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object SackValue : InventorySideGui(".* Sack") {

    private val gemstoneLevel = listOf("ROUGH", "FLAWED", "FINE")

    override val enabled get() = SackValueConfig.enabled

    override fun ContainerInitializedEvent.getLayout(): Layout? {
        val idsInInventory = containerItems.mapNotNullTo(mutableSetOf()) { it.getSkyBlockId() }

        val ids = when (title) {
            "Runes Sack" -> emptyList()
            "Gemstones Sack" -> idsInInventory.flatMap { id -> gemstoneLevel.map { id.replace("ROUGH", it) } }
            else -> idsInInventory
        }.ifEmpty { return null }

        return LayoutFactory.vertical {
            val sackEntries = SacksAPI.sackItems.filter { it.key in ids }.map {
                SackEntry(it.key, it.value)
            }.sortedByDescending { it.price }

            val title = LayoutFactory.horizontal {
                string(ChatUtils.ICON_SPACE_COMPONENT)
                string(-"inventory.sack_value")
                string(" - ")
                string(-SackValueConfig.priceSource.translationKey)
            }
            widget(title)

            LayoutFactory.vertical {
                sackEntries.forEach { (item, amount, price) ->
                    horizontal(alignment = MIDDLE) {
                        display(Displays.item(RepoItemsAPI.getItem(item), showTooltip = true))
                        textDisplay(" - ${amount.shorten()}") {
                            color = TextColor.DARK_GRAY
                            append(" (") {
                                color = TextColor.DARK_GRAY
                            }
                            append(price.shorten()) {
                                color = BETTER_GOLD
                            }
                            append(")") {
                                color = TextColor.DARK_GRAY
                            }
                        }
                    }
                }
            }.let {
                widget(
                    it.asScrollable(
                        it.width + 10, screen.containerHeight - 10 - title.height,
                        {
                            this.withScroll(oldList?.xScroll ?: 0, oldList?.yScroll ?: 0)
                            oldList = this
                        },
                    ),
                )
            }
        }
    }

    private data class SackEntry(val item: String, val amount: Int) {
        val price = when (SackValueConfig.priceSource) {
            SackValueConfig.PriceSource.BAZAAR -> Pricing.getPrice(item)
            SackValueConfig.PriceSource.NPC -> (ItemData.getNpcSellPrice(item) ?: 0).toLong()
        }.times(amount.toDouble()).toLong()

        operator fun component3() = price
    }
}
