package me.owdding.skyocean.features.inventory

import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.compat.REIRenderOverlayEvent
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.shorten
import me.owdding.lib.layouts.BackgroundWidget
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.garden.SackValueConfig
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen.asScrollable
import me.owdding.skyocean.utils.ChatUtils
import me.owdding.skyocean.utils.ChatUtils.BETTER_GOLD
import me.owdding.skyocean.utils.Utils.unaryMinus
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Inventory
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerInitializedEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.api.profile.items.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.ItemData
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.Pricing
import tech.thatgravyboat.skyblockapi.utils.extentions.containerHeight
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.right
import tech.thatgravyboat.skyblockapi.utils.extentions.top
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object SackValue {

    private val regex = ".* Sack".toRegex()
    private var oldList: LayoutWidget<*>? = null
    private var oldWidget: AbstractWidget? = null

    @Subscription
    fun reiBeingAStupidMod(event: REIRenderOverlayEvent) {
        with(oldWidget ?: return) {
            event.register(x, y, width, height)
        }
    }

    @Subscription
    fun onContainerClose(event: ContainerCloseEvent) {
        oldWidget = null
        oldList = null
    }

    // Used so that when Hypixel resends the entire screen we can
    // show the list before the items are resent so it doesn't
    // fall in and out.
    @Subscription
    fun onScreenInit(event: ScreenInitializedEvent) {
        if (!regex.matches(event.screen.title.stripped)) return
        val widget = this.oldWidget ?: return

        event.screen.addWidget(widget)
    }

    @Subscription(priority = Subscription.LOW)
    private fun ContainerInitializedEvent.onInvChange() {
        if (!regex.matches(screen.title.stripped)) return

        val idsInInventory = screen.menu.slots.filter { it.container !is Inventory }.mapNotNull { it.item.getSkyBlockId() }.toSet()

        val ids = when (title) {
            "Runes Sack" -> emptyList()
            "Gemstones Sack" -> idsInInventory.flatMap { id -> listOf("ROUGH", "FLAWED", "FINE").map { id.replace("ROUGH", it) } }
            else -> idsInInventory
        }.takeUnless { it.isEmpty() } ?: return

        val display = BackgroundWidget(
            SkyOcean.id("blank"),
            LayoutFactory.vertical {
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
                            display(Displays.item(RepoItemsAPI.getItem(item)))
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
                    widget(it.asScrollable(it.width + 10, screen.containerHeight - 10 - title.height, {
                        this.withScroll(oldList?.xScroll ?: 0, oldList?.yScroll ?: 0)
                        oldList = this
                    }))
                }
            },
            padding = 5,
        ).apply { this.setPosition(screen.right + 5, screen.top) }

        screen.addWidget(display)
    }

    private fun Screen.addWidget(widget: AbstractWidget) {
        oldWidget?.let { this.removeWidget(it) }
        oldWidget = widget
        this.addRenderableWidget(widget)
    }

    private data class SackEntry(val item: String, val amount: Int) {
        val price = when (SackValueConfig.priceSource) {
            SackValueConfig.PriceSource.BAZAAR -> Pricing.getPrice(item)
            SackValueConfig.PriceSource.NPC -> (ItemData.getNpcPrice(item) ?: 0).toLong()
        }.times(amount.toDouble()).toLong()

        operator fun component3() = price
    }
}
