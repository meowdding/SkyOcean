package me.owdding.skyocean.utils.rendering.widgets

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.utils.ListenableState
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.parseFormattedLong
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text

class SelectAmountWidget(private val itemId: SkyBlockId, private val onSubmit: (Long) -> Unit) : AbstractWidget(0, 0, 145, 64, CommonText.EMPTY) {

    private val state = ListenableState.of("1")
    private val children = mutableListOf<AbstractWidget>()

    private val topButtons = mutableListOf<AbstractWidget>()
    private val bottomButtons = mutableListOf<AbstractWidget>()
    private val inputField: AbstractWidget
    private val submitButton: AbstractWidget

    private val buttonWidth = 35
    private val buttonHeight = 20

    init {
        val increments = listOf(1L, 10L, 100L, 1000L)

        increments.forEach { amount ->
            val button = Widgets.button {
                it.withRenderer(WidgetRenderers.text(Text.of("+$amount")))
                it.withSize(buttonWidth, buttonHeight)
                it.withCallback { addAmount(amount) }
            }
            topButtons.add(button)
            children.add(button)
        }

        inputField = Widgets.textInput(state) {
            it.withSize(78, buttonHeight)
            it.withEnterCallback { submit() }
        }
        children.add(inputField)

        submitButton = Widgets.button {
            it.withRenderer(WidgetRenderers.text(Text.of("Submit")))
            it.withSize(buttonWidth, buttonHeight)
            it.withCallback { submit() }
        }
        children.add(submitButton)

        increments.forEach { amount ->
            val button = Widgets.button {
                it.withRenderer(WidgetRenderers.text(Text.of("-$amount")))
                it.withSize(buttonWidth, buttonHeight)
                it.withCallback { addAmount(-amount) }
            }
            bottomButtons.add(button)
            children.add(button)
        }
    }

    private fun addAmount(amount: Long) {
        val current = state.get().parseFormattedLong()
        val next = (current + amount).coerceAtLeast(1L)
        state.set(next.toString())
    }

    private fun submit() {
        val amount = state.get().parseFormattedLong()
        if (amount > 0) {
            onSubmit(amount)
        }
    }

    private fun updatePositions() {
        val spacing = 2

        for (i in 0 until 4) {
            topButtons[i].x = this.x + i * (buttonWidth + spacing)
            topButtons[i].y = this.y

            bottomButtons[i].x = this.x + i * (buttonWidth + spacing)
            bottomButtons[i].y = this.y + 2 * (buttonHeight + spacing)
        }

        inputField.x = this.x + 22
        inputField.y = this.y + buttonHeight + spacing

        submitButton.x = this.x + 3 * (buttonWidth + spacing)
        submitButton.y = this.y + buttonHeight + spacing
    }

    override fun setX(x: Int) {
        super.setX(x)
        updatePositions()
    }

    override fun setY(y: Int) {
        super.setY(y)
        updatePositions()
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        updatePositions()

        val iconX = this.x
        val iconY = this.y + 20 + 2
        graphics.item(itemId.toItem(), iconX + 2, iconY + 2)

        children.forEach { it.extractRenderState(graphics, mouseX, mouseY, a) }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (!this.active || !this.visible) return false
        return children.any { it.mouseClicked(event, doubleClick) } || super.mouseClicked(event, doubleClick)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        return children.any { it.mouseReleased(event) } || super.mouseReleased(event)
    }

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        return children.any { it.mouseDragged(event, dragX, dragY) } || super.mouseDragged(event, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        return children.any { it.mouseScrolled(mouseX, mouseY, scrollX, scrollY) } || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        return children.any { it.keyPressed(event) } || super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        return children.any { it.charTyped(event) } || super.charTyped(event)
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput) {}
}
