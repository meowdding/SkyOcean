package me.owdding.skyocean.features.hotkeys

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.ui.UITexts
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.layouts.withPadding
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.utils.components.CatppuccinColors
import me.owdding.skyocean.utils.extensions.asSprite
import me.owdding.skyocean.utils.extensions.asWidget
import me.owdding.skyocean.utils.extensions.createButton
import me.owdding.skyocean.utils.extensions.middleLeft
import me.owdding.skyocean.utils.extensions.middleRight
import me.owdding.skyocean.utils.extensions.withPadding
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderPipelines
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.Text
import kotlin.math.max

private const val PADDING = 5
private const val HEADER_HEIGHT = PADDING * 2

class CreateCategoryModal(
    val parent: Screen?,
    val sectionWidth: Int,
    val callback: (name: String, by: String) -> Unit,
) : Overlay(parent), IgnoreHotkeyInputs {
    private var layout: Layout = LayoutFactory.empty()

    override fun init() {
        super.init()
        val modalWidth = max(sectionWidth, 100)

        val nameState = ListenableState.of("")
        val madeByState = ListenableState.of(McPlayer.name)

        this.layout = Layouts.column()
            .withGap(PADDING)
            .withChild(
                Widgets.frame()
                    .withTexture(id("hotkey/header"))
                    .withContents { contents: FrameLayout ->
                        contents.addChild(
                            Widgets.labelled(
                                this.font,
                                Text.of("Create Category"),
                                CatppuccinColors.Mocha.lavenderColor,
                                createButton(
                                    texture = null,
                                    icon = UIIcons.X,
                                    click = ::onClose,
                                    hover = UITexts.BACK,
                                    color = CatppuccinColors.Mocha.lavenderColor,
                                ),
                            ).withSize(modalWidth, HEADER_HEIGHT + PADDING * 2 - 2)
                                .withEqualSpacing(Orientation.HORIZONTAL)
                                .withPadding(bottom = 2),
                        )
                    }
                    .withStretchToContentSize()
                    .withContentMargin(PADDING),
            )
            .withChildren(
                LayoutFactory.vertical(PADDING) {
                    LayoutFactory.frame(modalWidth) {
                        val nameWidget = Text.of("Name", CatppuccinColors.Mocha.text).asWidget().withPadding(PADDING)
                        nameWidget.add(middleLeft)

                        val input = Widgets.textInput(nameState) { _ -> }
                        input.withPlaceholder("Name")
                        input.withTexture(id("hotkey/inset").asSprite())
                        input.withSize(modalWidth - nameWidget.width - PADDING, PADDING * 3)
                        input.withPlaceholderColor(CatppuccinColors.Mocha.overlay1Color)
                        input.withTextColor(CatppuccinColors.Mocha.textColor)
                        input.withPadding(right = PADDING).add(middleRight)
                    }.add()
                    LayoutFactory.frame(modalWidth) {
                        val nameWidget = Text.of("Made by", CatppuccinColors.Mocha.text).asWidget().withPadding(PADDING)
                        nameWidget.add(middleLeft)

                        val input = Widgets.textInput(madeByState) { _ -> }
                        input.withSize(modalWidth - nameWidget.width - PADDING, PADDING * 3)
                        input.withPlaceholderColor(CatppuccinColors.Mocha.overlay1Color)
                        input.withTexture(id("hotkey/inset").asSprite())
                        input.withTextColor(CatppuccinColors.Mocha.textColor)
                        input.withPadding(right = PADDING).add(middleRight)
                    }.add()
                },
            )
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, PADDING * 6)
                    .withTexture(id("hotkey/header"))
                    .withContents { contents: FrameLayout ->
                        contents.addChild(
                            createButton(
                                text = Text.of("Close"),
                                texture = UIConstants.DANGER_BUTTON,
                                width = modalWidth / 2 - PADDING * 2,
                                height = PADDING * 4,
                                click = {
                                    this.onClose()
                                },
                            ),
                        )
                        contents.addChild(
                            createButton(
                                text = Text.of("Confirm"),
                                texture = UIConstants.PRIMARY_BUTTON,
                                width = modalWidth / 2 - PADDING * 2,
                                height = PADDING * 4,
                                click = {
                                    val name = nameState.get().takeUnless(String::isEmpty) ?: "Unnamed"
                                    val madeBy = madeByState.get().takeUnless(String::isEmpty) ?: McPlayer.name
                                    callback(name, madeBy)
                                    this.onClose()
                                },
                            ),
                        )
                    }
                    .withEqualSpacing(Orientation.HORIZONTAL)
                    .withContentMargin(PADDING),
            )
            .build { widget: AbstractWidget -> this.addRenderableWidget(widget) }

        FrameLayout.centerInRectangle(this.layout, this.rectangle)
    }


    override fun renderBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick)
        this.renderTransparentBackground(graphics)

        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            id("hotkey/background"),
            this.layout.x, this.layout.y,
            this.layout.width, this.layout.height,
        )
    }
}
