package me.owdding.skyocean.features.inventory

import me.owdding.ktmodules.Module
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withPadding
import me.owdding.lib.extensions.shorten
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen.asScrollable
import me.owdding.skyocean.utils.ChatUtils
import me.owdding.skyocean.utils.Utils.unaryMinus
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import tech.thatgravyboat.skyblockapi.api.area.hub.BazaarAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.InventoryTitle
import tech.thatgravyboat.skyblockapi.api.events.render.RenderScreenForegroundEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.profile.items.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.containerHeight
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.right
import tech.thatgravyboat.skyblockapi.utils.extentions.top
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object SackValue {

    private var gold = 0xfc6f03

    private var display: Display? = null

    private val regex = ".* Sack".toRegex()

    @Subscription(priority = 1)
    fun onInvChange(event: InventoryChangeEvent) {
        if (!regex.matches(event.title)) {
            display = null
            return
        }

        val height = event.screen.containerHeight

        display = Displays.background(
            SkyOcean.id("blank"),
            DisplayFactory.vertical {
                val ids = event.itemStacks.mapNotNull { it.getSkyBlockId() }
                val sackEntries = SacksAPI.sackItems.filter { it.key in ids }

                horizontal {
                    string(ChatUtils.ICON_SPACE_COMPONENT)
                    string(-"inventory.sack_value")
                }

                sackEntries.forEach { (item, amount) ->
                    LayoutFactory.horizontal(alignment = MIDDLE) {
                        display(Displays.item(RepoItemsAPI.getItem(item)))
                        textDisplay(" - ${amount.shorten()}") {
                            color = TextColor.DARK_GRAY
                            append(" (") {
                                color = TextColor.DARK_GRAY
                            }
                            append(BazaarAPI.getProduct(item)?.sellPrice?.shorten() ?: "0") {
                                color = gold
                            }
                            append(")") {
                                color = TextColor.DARK_GRAY
                            }
                        }
                    }.let {
                        widget(it.asScrollable(it.width, height - 10))
                    }
                }

            }.withPadding(5),
        )
    }

    @Subscription
    @InventoryTitle(".* Sack")
    fun onRender(event: RenderScreenForegroundEvent) {
        val container = event.screen as? AbstractContainerScreen<*> ?: return
        display?.render(event.graphics, container.right + 5, container.top)
    }

}
