package me.owdding.skyocean.features.misc.buttons

import com.teamresourceful.resourcefullib.common.color.Color
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.string.TextWidget
import earth.terrarium.olympus.client.components.textbox.TextBox
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.utils.State
import me.owdding.lib.layouts.setPos
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.misc.ButtonConfig
import me.owdding.skyocean.config.features.misc.Buttons
import net.minecraft.client.gui.GuiGraphics
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

    val textWidget: TextWidget = Widgets.text("Button Configuration").withColor(Color.DEFAULT)
    val itemState: State<String> = State.of("")
    val itemWidget: TextBox = Widgets.textInput(itemState)
    val commandState: State<String> = State.of("")
    val commandWidget: TextBox = Widgets.textInput(commandState)
    val titleState: State<String> = State.of("")
    val titleWidget: TextBox = Widgets.textInput(titleState)
    val tooltipState: State<String> = State.of("")
    val tooltipWidget: TextBox =  Widgets.textInput(tooltipState)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean = false
    @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE") override fun slotClicked(slot: Slot?, slotId: Int, mouseButton: Int, type: ClickType) {}
    override fun renderSlots(guiGraphics: GuiGraphics) {}
    override fun onRecipeBookButtonClick() {}
    // Crazy workaround, why the fuck is the whole inventory tied to the recipe book holy shit
    override fun getRecipeBookButtonPosition(): ScreenPosition? = ScreenPosition(1000, 1000)

    override fun renderTooltip(guiGraphics: GuiGraphics, x: Int, y: Int) {}

    fun refresh(selectedButtonIndex: Int) {
        if (selectedButtonIndex == -1) {
            this.selectedButtonIndex = selectedButtonIndex
            itemState.set("")
            commandState.set("")
            titleState.set("")
            tooltipState.set("")
            return
        }
        textWidget.active = true
        itemWidget.active = true
        commandWidget.active = true
        titleWidget.active = true
        tooltipWidget.active = true

        this.selectedButtonIndex = selectedButtonIndex
        this.selectedButton = Buttons.buttons[selectedButtonIndex]

        itemState.set(selectedButton?.item)
        commandState.set(selectedButton?.command)
        titleState.set(selectedButton?.title)
        tooltipState.set(selectedButton?.tooltip)
    }

    override fun onClose() {
        if (itemWidget.isHovered || commandWidget.isHovered || titleWidget.isHovered || tooltipWidget.isHovered) return
        SkyOcean.config.save()
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
        itemWidget.withPlaceholder("Item ID / Skyblock ID")
        itemWidget.active = false
        itemWidget.withEnterCallback {
            selectedButton?.item = it
        }

        commandWidget.withSize(width, height)
        commandWidget.withPlaceholder("Command")
        commandWidget.active = false
        commandWidget.withEnterCallback {
            selectedButton?.command = it
        }

        titleWidget.withSize(width, height)
        titleWidget.withPlaceholder("Screen Title")
        titleWidget.active = false
        titleWidget.withEnterCallback {
            selectedButton?.title = it
        }

        tooltipWidget.withSize(width, height)
        tooltipWidget.withPlaceholder("Button Tooltip")
        tooltipWidget.active = false
        tooltipWidget.withEnterCallback {
            selectedButton?.tooltip = it
        }

        column.withChildren(textWidget, itemWidget, commandWidget, titleWidget, tooltipWidget)
        column.arrangeElements()
        column.visitWidgets(::addRenderableWidget)
    }

    override fun rebuildWidgets() {
        refresh(-1)
        super.rebuildWidgets()
    }
}
