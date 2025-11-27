package me.owdding.skyocean.features.inventory.buttons

import com.teamresourceful.resourcefullib.common.color.Color
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.components.string.TextWidget
import earth.terrarium.olympus.client.components.textbox.TextBox
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.utils.State
import me.owdding.lib.layouts.setPos
import me.owdding.skyocean.config.Config
import me.owdding.skyocean.config.features.inventory.ButtonConfig
import me.owdding.skyocean.config.features.inventory.Buttons
import me.owdding.skyocean.mixins.AbstractRecipeBookScreenAccessor
import me.owdding.skyocean.utils.Utils.resetCursor
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.navigation.ScreenPosition
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.left
import tech.thatgravyboat.skyblockapi.utils.extentions.top

class ButtonConfigScreen(val previousScreen: Screen?) : InventoryScreen(McPlayer.self) {

    var selectedButtonIndex = -1
    private var selectedButton: ButtonConfig? = null

    val textWidget: TextWidget = Widgets.text(+"skyocean.inventory.buttons.configuration")
    val itemState: State<String> = State.of("")
    val itemWidget: TextBox = Widgets.textInput(itemState)
    val commandState: State<String> = State.of("")
    val commandWidget: TextBox = Widgets.textInput(commandState)
    val titleState: State<String> = State.of("")
    val titleWidget: TextBox = Widgets.textInput(titleState)
    val tooltipState: State<String> = State.of("")
    val tooltipWidget: TextBox = Widgets.textInput(tooltipState)
    val disableRenderer = WidgetRenderers.text<Button>(+"skyocean.inventory.buttons.disable").withColor(Color(0xFF0000))
    val enableRenderer = WidgetRenderers.text<Button>(+"skyocean.inventory.buttons.enable").withColor(Color(0x00FF00))
    val disableButton: Button = Widgets.button().withRenderer(disableRenderer)
    val resetButton: Button = Widgets.button().withRenderer(WidgetRenderers.text(+"skyocean.inventory.buttons.reset"))

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean = false
    @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE")
    override fun slotClicked(slot: Slot?, slotId: Int, mouseButton: Int, type: ClickType) {
    }

    override fun renderSlots(guiGraphics: GuiGraphics) {}
    override fun onRecipeBookButtonClick() {}

    // Crazy workaround, why the fuck is the whole inventory tied to the recipe book holy shit
    override fun getRecipeBookButtonPosition(): ScreenPosition = ScreenPosition(1000, 1000)
    override fun showsActiveEffects(): Boolean = false

    override fun renderTooltip(guiGraphics: GuiGraphics, x: Int, y: Int) {}

    fun refresh(selectedButtonIndex: Int) {
        itemWidget.resetCursor()
        commandWidget.resetCursor()
        titleWidget.resetCursor()
        tooltipWidget.resetCursor()
        this.children().filterIsInstance<AbstractWidget>().forEach {
            it.isFocused = false
        }
        if (selectedButtonIndex == -1) {
            this.selectedButtonIndex = selectedButtonIndex
            this.selectedButton = null
            textWidget.active = false
            itemWidget.active = false
            commandWidget.active = false
            titleWidget.active = false
            tooltipWidget.active = false
            disableButton.active = false
            resetButton.active = false
            itemState.set("")
            commandState.set("")
            titleState.set("")
            tooltipState.set("")
            disableButton.withRenderer(disableRenderer)
            return
        }
        textWidget.active = true
        itemWidget.active = true
        commandWidget.active = true
        titleWidget.active = true
        tooltipWidget.active = true
        disableButton.active = true
        resetButton.active = true

        this.selectedButtonIndex = selectedButtonIndex
        this.selectedButton = Buttons.buttons[selectedButtonIndex]

        disableButton.withRenderer(if (selectedButton!!.disabled) enableRenderer else disableRenderer)

        itemState.set(selectedButton?.item)
        commandState.set(selectedButton?.command)
        titleState.set(selectedButton?.title)
        tooltipState.set(selectedButton?.tooltip)
    }

    override fun onClose() {
        if (itemWidget.isHovered || commandWidget.isHovered || titleWidget.isHovered || tooltipWidget.isHovered) return
        if (selectedButtonIndex != -1) {
            refresh(-1)
            return
        }
        Config.save()
        McClient.setScreen(previousScreen)
    }

    override fun init() {
        super.init()

        val column = Layouts.column()
        val width = 130
        val height = 20
        column.setPos(this.left - width, this.top)

        textWidget.active = false

        itemWidget.withSize(width, height)
        itemWidget.withPlaceholder((+"skyocean.inventory.buttons.item").string)
        itemWidget.active = false
        itemWidget.withChangeCallback {
            selectedButton?.item = it
        }

        commandWidget.withSize(width, height)
        commandWidget.withPlaceholder((+"skyocean.inventory.buttons.command").string)
        commandWidget.active = false
        commandWidget.withChangeCallback {
            selectedButton?.command = it
        }

        titleWidget.withSize(width, height)
        titleWidget.withPlaceholder((+"skyocean.inventory.buttons.screen_title").string)
        titleWidget.active = false
        titleWidget.withChangeCallback {
            selectedButton?.title = it
        }

        tooltipWidget.withSize(width, height)
        tooltipWidget.withPlaceholder((+"skyocean.inventory.buttons.tooltip").string)
        tooltipWidget.active = false
        tooltipWidget.withChangeCallback {
            selectedButton?.tooltip = it
        }

        disableButton.withSize(width, height)
        disableButton.active = false
        disableButton.withCallback {
            if (selectedButton != null) {
                selectedButton!!.disabled = !selectedButton!!.disabled
                disableButton.withRenderer(if (selectedButton!!.disabled) enableRenderer else disableRenderer)
            }
        }

        resetButton.withSize(width, height)
        resetButton.active = false
        resetButton.withCallback {
            selectedButton?.reset()
            refresh(selectedButtonIndex)
        }

        val guide = Widgets.text((+"skyocean.inventory.buttons.select_button"))
        guide.setSize(width, height)

        column.withChildren(textWidget, itemWidget, commandWidget, titleWidget, tooltipWidget, disableButton, resetButton, guide)
        column.arrangeElements()
        column.visitWidgets(::addRenderableWidget)

        val recipebook = (this as AbstractRecipeBookScreenAccessor).`skyocean$getRecipeBookComponent`()
        if (recipebook.isVisible) recipebook.toggleVisibility()
    }

    override fun rebuildWidgets() {
        refresh(-1)
        super.rebuildWidgets()
    }
}
