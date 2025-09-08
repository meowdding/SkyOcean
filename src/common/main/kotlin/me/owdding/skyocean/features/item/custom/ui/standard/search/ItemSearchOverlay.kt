package me.owdding.skyocean.features.item.custom.ui.standard.search

import com.google.common.primitives.Ints
import com.teamresourceful.resourcefullib.common.utils.TriState
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.ClearableGridLayout
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.displays.Displays
import me.owdding.lib.overlays.Rect
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient

class ItemSearchOverlay(
    screen: Screen?,
    widget: AbstractWidget,
    private val base: ItemStack,
) : Overlay(screen) {

    private val query = ListenableState.of("")
    private val bounds = Rect(widget.x, widget.y, widget.width, widget.height)
    private val entries by lazy {
        LayoutWidget(ClearableGridLayout()).also {
            it.withPosition(bounds.x, bounds.y + bounds.height)
            it.withContentMargin(1)
            it.withScrollableY(TriState.UNDEFINED)
            it.withTexture(UIConstants.LIST_BG)
            it.withScrollbarBackground(UIConstants.MODAL_INSET)
            it.withLayoutCallback { widget, layout ->
                widget.withSize(
                    bounds.width,
                    Ints.min(
                        layout.height,
                        this.height - (bounds.y + bounds.height),
                        10 * 16
                    )
                )
            }
        }
    }

    init {
        this.query.registerListener(this::update)
    }

    override fun init() {
        super.init()

        Widgets.textInput(query) {
            it.withSize(bounds.width, bounds.height)
            it.withPosition(bounds.x, bounds.y)
        }.let(this::addRenderableWidget)

        this.addRenderableWidget(this.entries)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.render(graphics, mouseX, mouseY, partialTicks)
    }

    override fun resize(minecraft: Minecraft, width: Int, height: Int) {
        super.resize(minecraft, width, height)
        McClient.runNextTick(this::onClose)
    }

    fun update(query: String) {
        val entries = ItemSearchEntries.ENTRIES.stream()
            .filter { it.matches(query) }
            .map { entry ->
                val item = Displays.center(
                    16, 16,
                    Displays.item(entry.resolve(this.base), 12, 12)
                )
                Widgets.button {
                    it.withSize(bounds.width - 2, 16)
                    it.withTexture(UIConstants.LIST_ENTRY)
                    it.withRenderer(
                        WidgetRenderers.layered(
                            { graphics, ctx, _ -> item.render(graphics, ctx.x, ctx.y) },
                            WidgetRenderers.text<Button>(entry.name)
                                .withColor(MinecraftColors.WHITE)
                                .withPaddingLeft(16),
                        ),
                    )
                    it.withCallback {
                        // TODO apply some how?
                        this.onClose()
                    }
                }
            }
            .limit(100)
            .toArray { arrayOfNulls<Button>(it) }

        this.entries.withContents { layout ->
            layout.clear()
            for ((index, value) in entries.withIndex()) {
                layout.addChild(value, index, 0)
            }
        }
    }
}
