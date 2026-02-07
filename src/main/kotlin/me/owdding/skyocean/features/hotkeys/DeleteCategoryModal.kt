//? > 1.21.8 {
package me.owdding.skyocean.features.hotkeys

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.ui.UITexts
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.features.hotkeys.system.HotkeyCategory
import me.owdding.skyocean.features.hotkeys.system.HotkeyManager
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.utils.chat.CatppuccinColors
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
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

private const val PADDING = 5
private const val HEADER_HEIGHT = PADDING * 2

class DeleteCategoryModal(
    val parent: Screen?,
    val sectionWidth: Int,
    val category: HotkeyCategory,
    val callback: () -> Unit,
) : Overlay(parent), IgnoreHotkeyInputs {
    private var layout: Layout = LayoutFactory.empty()

    override fun init() {
        super.init()
        val content = LayoutFactory.vertical(PADDING) {
            Text.of {
                this.color = CatppuccinColors.Mocha.text
                append("Are you sure you want to ")
                append("delete ")
                append("'")
                append(category.name, CatppuccinColors.Mocha.sky)
                append("'?")
            }.asWidget().withPadding(PADDING).add()
            Text.of("This action can't be undone!", CatppuccinColors.Mocha.red).asWidget().withPadding(PADDING, top = 0).add()
        }
        val modalWidth = content.width

        this.layout = Layouts.column()
            .withGap(PADDING)
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, HEADER_HEIGHT + PADDING * 2)
                    .withTexture(id("hotkey/header"))
                    .withContents { contents: FrameLayout ->
                        contents.addChild(
                            LayoutFactory.frame(modalWidth - PADDING * 2, HEADER_HEIGHT + PADDING * 2) {
                                Widgets.text(Text.of("Delete Category")).withColor(CatppuccinColors.Mocha.lavenderColor).add(middleLeft)
                                createButton(
                                    texture = null,
                                    icon = UIIcons.X,
                                    click = ::onClose,
                                    color = CatppuccinColors.Mocha.lavenderColor,
                                    hover = UITexts.BACK,
                                ).add(middleRight)
                            }.asWidget().withPadding(PADDING, bottom = 2, top = 0),
                        )
                    },
            )
            .withChildren(content)
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, PADDING * 6)
                    .withTexture(id("hotkey/header"))
                    .withContents { contents: FrameLayout ->
                        contents.addChild(
                            createButton(
                                text = Text.of("Cancel", CatppuccinColors.Mocha.base),
                                texture = UIConstants.BUTTON,
                                width = modalWidth / 2 - PADDING * 2,
                                height = PADDING * 4,
                                click = {
                                    this.onClose()
                                },
                            ),
                        )
                        contents.addChild(
                            createButton(
                                text = Text.of("Delete"),
                                texture = UIConstants.DANGER_BUTTON,
                                width = modalWidth / 2 - PADDING * 2,
                                height = PADDING * 4,
                                click = {
                                    HotkeyManager.deleteCategory(category)
                                    callback()
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
//?}
