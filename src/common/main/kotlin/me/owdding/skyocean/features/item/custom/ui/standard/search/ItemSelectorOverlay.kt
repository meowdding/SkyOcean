package me.owdding.skyocean.features.item.custom.ui.standard.search

import com.google.common.primitives.Ints
import com.teamresourceful.resourcefullib.common.utils.TriState
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.ClearableGridLayout
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.displays.Displays
import me.owdding.lib.overlays.Rect
import me.owdding.lib.platform.screens.MouseButtonEvent
import me.owdding.lib.platform.screens.Overlay
import me.owdding.skyocean.features.item.custom.CustomItems.getOrCreateStaticData
import me.owdding.skyocean.features.item.custom.ui.standard.StandardCustomizationUi
import me.owdding.skyocean.utils.rendering.ExtraWidgetRenderers
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient

class ItemSelectorOverlay(
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
                        this.height - (bounds.y + bounds.height) - 4,
                        10 * 16,
                    ),
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
        update(query.get())
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
                Widgets.button {
                    it.withSize(bounds.width - 2, 16)
                    it.withTexture(UIConstants.LIST_ENTRY)
                    it.withRenderer(resolveRenderer(entry.resolve(this.base), entry.name))
                    it.withCallback {
                        entry.apply {
                            base.getOrCreateStaticData()?.applyToData()
                        }
                        StandardCustomizationUi.buttonClick()
                        this.onClose()
                        StandardCustomizationUi.anyUpdated = true
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

    override fun mouseClicked(mouseEvent: MouseButtonEvent, doubleClicked: Boolean): Boolean {
        if (!super.mouseClicked(mouseEvent, doubleClicked)) {
            onClose()
        }
        return true
    }

    companion object {
        fun resolveRenderer(itemStack: ItemStack, itemModelEntry: ModelSearchEntry, size: Int = 16) =
            resolveRenderer(itemModelEntry.resolve(itemStack), itemModelEntry.name, size)

        fun getItemDisplay(itemStack: ItemStack, size: Int) = Displays.center(size, size, Displays.item(itemStack, size - 4, size - 4))
        fun resolveRenderer(itemStack: ItemStack, name: Component, size: Int = 16): WidgetRenderer<Button?>? {
            val item = Displays.center(
                size, size,
                Displays.item(itemStack, size - 4, size - 4),
            )
            return WidgetRenderers.layered(
                ExtraWidgetRenderers.display(item),
                WidgetRenderers.text<Button>(name)
                    .withColor(MinecraftColors.WHITE)
                    .withLeftAlignment()
                    .withPaddingLeft(size),
            )
        }
    }
}
