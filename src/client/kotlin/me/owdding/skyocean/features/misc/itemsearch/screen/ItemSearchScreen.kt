package me.owdding.skyocean.features.misc.itemsearch.screen

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.builder.RIGHT
import me.owdding.lib.displays.*
import me.owdding.lib.displays.Displays.background
import me.owdding.lib.extensions.rightPad
import me.owdding.skyocean.SkyOcean.olympus
import me.owdding.skyocean.features.misc.itemsearch.soures.ItemSources
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.helpers.McClient

object ItemSearchScreen : SkyOceanScreen() {

    override fun init() {
        super.init()
        val width = (width / 3).coerceAtLeast(100) + 50
        val height = (height / 3).coerceAtLeast(100) + 50

        Displays.layered(
            background(olympus("buttons/normal"), width, height),
            background(olympus("buttons/dark/normal"), width, 26),
        ).asWidget().center().applyAsRenderable()


        LayoutFactory.frame(width, height) {
            vertical {
                vertical(alignment = RIGHT) {
                    spacer(width)
                    LayoutFactory.horizontal {
                        spacer(height = 24)
                        val input = Widgets.textInput(ListenableState.of("")) { box ->
                            box.withChangeCallback(::refreshSearch)
                        }
                        input.withPlaceholder("Search...")
                        input.withSize(100, 20)
                        input.add {
                            alignVerticallyMiddle()
                        }
                        spacer(width = 2)
                    }.add()
                }
                spacer(height = 2)
                vertical {
                    horizontal {
                        spacer(width = 4, height - 26)
                        vertical(alignment = MIDDLE) {
                            buildItems(width - 10, height - 26)
                                .asWidget()
                                .asScrollable(width - 5, height = height - 29, allwaysShowScrollBar = true)
                                .add()
                        }
                    }
                }
            }
        }.center().applyLayout()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
        //rebuildWidgets()
        super.render(graphics, mouseX, mouseY, f)
    }

    fun buildItems(width: Int, height: Int): Display {
        val columns = (width - 5) / 20
        val rows = (height - 5) / 20
        val items = ItemSources.getAllItems().map { (itemStack, context) ->
            Displays.item(itemStack, showStackSize = true)
                .withTooltip {
                    getTooltipFromItem(McClient.self, itemStack).forEach(::add)
                    space()
                    context.collectLines().forEach(::add)
                }.withPadding(2)
        }.rightPad(columns * rows, background(0xFF000000u, Displays.empty(20, 20))).chunked(columns)

        return ExtraDisplays.inventoryBackground(columns, items.size, items.asTable().withPadding(2)).withPadding(top = 5, bottom = 5)
    }

    fun refreshSearch(search: String) {

    }

    override fun renderBlurredBackground() {

    }
}
