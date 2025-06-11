package me.owdding.skyocean.features.inventory

import earth.terrarium.olympus.client.components.base.BaseParentWidget
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.shorten
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen.asScrollable
import me.owdding.skyocean.utils.ChatUtils
import me.owdding.skyocean.utils.ChatUtils.BETTER_GOLD
import me.owdding.skyocean.utils.ContainerBypass
import me.owdding.skyocean.utils.Utils.unaryMinus
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import tech.thatgravyboat.skyblockapi.api.area.hub.BazaarAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.profile.items.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
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
    private var oldWidget: AbstractWidget? = null

    @Subscription(priority = 1)
    fun onInvChange(event: InventoryChangeEvent) {
        if (!regex.matches(event.screen.title.stripped)) return
        if (event.isSkyBlockFiller || event.isInBottomRow) return

        val screen = event.screen
        val height = screen.containerHeight

        val idsInInventory = screen.menu.slots.filter { it.container !is Inventory }.mapNotNull { it.item.getSkyBlockId() }.toSet()

        val ids = when (event.title) {
            "Runes Sack" -> emptyList()
            "Gemstones Sack" -> idsInInventory.flatMap { id -> listOf("ROUGH", "FLAWED", "FINE").map { id.replace("ROUGH", it) } }
            else -> idsInInventory
        }.takeUnless { it.isEmpty() } ?: return

        val display = BackgroundWidget(
            SkyOcean.id("blank"),
            LayoutFactory.vertical {
                val sackEntries = SacksAPI.sackItems.filter { it.key in ids }.map {
                    SackEntry(it.key, it.value, BazaarAPI.getProduct(it.key)?.sellPrice?.times(it.value) ?: 0.0)
                }.sortedByDescending { it.price }

                val title = LayoutFactory.horizontal {
                    string(ChatUtils.ICON_SPACE_COMPONENT)
                    string(-"inventory.sack_value")
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
                    widget(it.asScrollable(it.width + 10, height - 10 - title.height))
                }
            },
            padding = 5,
        ).apply { this.setPosition(screen.right + 5, screen.top) }

        oldWidget?.let { screen.removeWidget(it) }
        oldWidget = display
        event.screen.addRenderableWidget(display)
    }

    private data class SackEntry(val item: String, val amount: Int, val price: Double)
}

class BackgroundWidget(val background: ResourceLocation, widget: LayoutElement, val padding: Int = 0) : BaseParentWidget(), ContainerBypass {
    val body = widget.asWidget()

    init {
        this.addRenderableWidget(this.body)
    }

    override fun getWidth() = body.width + padding * 2
    override fun getHeight() = body.height + padding * 2

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        graphics.blitSprite(RenderType::guiTextured, background, this.x, this.y, body.width + padding * 2, body.height + padding * 2)

        body.setPosition(this.x + padding, this.y + padding)

        super.renderWidget(graphics, mouseX, mouseY, partialTicks)
    }
}
