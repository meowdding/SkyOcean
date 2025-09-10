package me.owdding.skyocean.features.item.custom.ui.standard

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.components.string.MultilineTextWidget
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.UITexts
import earth.terrarium.olympus.client.utils.Orientation
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import kotlin.math.max

const val PADDING: Int = 5
const val BUTTON_GAP: Int = 5
const val CONTENT_GAP: Int = 5
const val HEADER_HEIGHT: Int = 11

class ItemSelectorModal(val builder: ItemCustomizationModalBuilder, parent: Screen?) : Overlay(parent) {
    private var layout: Layout? = null

    override fun init() {
        super.init()

        val actions = this.builder.actions
        val actionsHeight: Int = actions.stream().mapToInt(AbstractWidget::getHeight).max().orElse(20)
        val actionsWidth: Int = max(
            actions.sumOf { it.width } + (actions.size - 1) * BUTTON_GAP,
            this.builder.minWidth,
        )

        val content = this.builder.content.stream().map { f -> f.apply(actionsWidth) }.toList()
        val minContentHeight: Int = this.builder.minHeight - HEADER_HEIGHT - actionsHeight - PADDING * 4
        val contentHeight: Int = content.stream().mapToInt(AbstractWidget::getHeight).sum() + content.size * CONTENT_GAP
        val contentWidth: Int = content.stream().mapToInt(AbstractWidget::getWidth).max().orElse(10)

        val modalWidth: Int = max(contentWidth, actionsWidth) + PADDING * 2

        val closeButton = Widgets.button()
            .withTexture(null)
            .withRenderer(WidgetRenderers.sprite<Button?>(UIConstants.MODAL_CLOSE))
            .withCallback { this.onClose() }
            .withTooltip(UITexts.BACK)
            .withSize(11, 11)

        val contentLayout = Layouts.column().withGap(CONTENT_GAP)
        content.forEach { widget ->
            contentLayout.withChild(
                Layouts.row()
                    .withChild(SpacerElement.width(PADDING))
                    .withChild(widget)
                    .withChild(SpacerElement.width(PADDING)),
            )
        }
        if (contentHeight < minContentHeight) {
            contentLayout.withChild(SpacerElement.height(minContentHeight - contentHeight - CONTENT_GAP))
        }

        this.layout = Layouts.column()
            .withGap(PADDING)
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, HEADER_HEIGHT + PADDING * 2)
                    .withTexture(UIConstants.MODAL_HEADER)
                    .withContents {
                        it.addChild(
                            Widgets.labelled(this.font, this.builder.title, closeButton).withEqualSpacing(Orientation.HORIZONTAL),
                        )
                    }
                    .withContentFill()
                    .withContentMargin(PADDING),
            )
            .withChildren(contentLayout)
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, actionsHeight + PADDING * 2)
                    .withTexture(UIConstants.MODAL_FOOTER)
                    .withContents {
                        val eachWidth = (modalWidth - PADDING * 2 - PADDING * (actions.size - 1)) / actions.size
                        actions.forEach { action ->
                            action.width = eachWidth
                            it.addChild(action)
                        }
                    }
                    .withEqualSpacing(Orientation.HORIZONTAL)
                    .withContentMargin(PADDING),
            )
            .build { widget: AbstractWidget -> this.addRenderableWidget(widget) }

        FrameLayout.centerInRectangle(this.layout!!, this.rectangle)
    }

    override fun renderBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val layout = layout ?: return
        super.renderBackground(graphics, mouseX, mouseY, partialTick)
        this.renderTransparentBackground(graphics)

        graphics.drawSprite(
            UIConstants.MODAL,
            layout.x - 1, layout.y - 1,
            layout.width + 2, layout.height + 2,
        )
    }

    override fun resize(mc: Minecraft, width: Int, height: Int) {

        this.width = width
        this.height = height
        this.repositionElements()
    }
}

class ItemCustomizationModalBuilder {
    internal val actions: MutableList<AbstractWidget> = ArrayList()
    internal val content: MutableList<Int2ObjectFunction<AbstractWidget>> = ArrayList()
    internal var title: Component? = null
    internal var minWidth = 150
    internal var minHeight = 100

    fun withTitle(title: Component?): ItemCustomizationModalBuilder {
        this.title = title
        return this
    }

    fun withMinWidth(minWidth: Int): ItemCustomizationModalBuilder {
        this.minWidth = minWidth
        return this
    }

    fun withMinHeight(minHeight: Int): ItemCustomizationModalBuilder {
        this.minHeight = minHeight
        return this
    }

    fun withContent(widget: Int2ObjectFunction<AbstractWidget>): ItemCustomizationModalBuilder {
        this.content.add(widget)
        return this
    }

    fun withContent(widget: BaseWidget): ItemCustomizationModalBuilder {
        this.content.add(Int2ObjectFunction { width: Int -> widget.withSize(width, widget.getHeight()) })
        return this
    }

    fun withContent(text: Component?): ItemCustomizationModalBuilder {
        this.content.add(Int2ObjectFunction { i: Int -> (MultilineTextWidget(i, text, Minecraft.getInstance().font)).alignLeft() })
        return this
    }

    fun withAction(widget: AbstractWidget): ItemCustomizationModalBuilder {
        this.actions.add(widget)
        return this
    }

    fun open() {
        Minecraft.getInstance().setScreen(ItemSelectorModal(this, McScreen.self))
    }
}
