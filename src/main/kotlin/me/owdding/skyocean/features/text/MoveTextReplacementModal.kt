package me.owdding.skyocean.features.text

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.ui.UITexts
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asButtonLeft
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.features.hotkeys.ConditionalHotkeyScreen.SPACER
import me.owdding.skyocean.features.hotkeys.IgnoreHotkeyInputs
import me.owdding.skyocean.features.hotkeys.system.Hotkey
import me.owdding.skyocean.features.hotkeys.system.HotkeyCategory
import me.owdding.skyocean.features.hotkeys.system.HotkeyManager
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.extensions.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined
import kotlin.math.min

private const val PADDING = 5
private const val HEADER_HEIGHT = PADDING * 2

class MoveTextReplacementModal(
    parent: Screen? = McScreen.self,
    val hotkey: TextReplacement,
) : Overlay(parent), IgnoreHotkeyInputs {
    var lastScrollGetter = { 0 }

    private var layout: Layout = LayoutFactory.empty()

    fun createEntry(preset: TextReplacementCategory, width: Int, height: Int) = LayoutFactory.frame(width, height) {
        Displays.text(Text.of(preset.name) {
            underlined = hotkey.category == preset.identifier
        }, color = { ARGB.opaque(if (hotkey.category == preset.identifier) CatppuccinColors.Mocha.green else CatppuccinColors.Mocha.sky).toUInt() }).asButtonLeft {
            hotkey.category = preset.identifier
            onClose()
            HotkeyManager.save()
        }.withPadding(left = SPACER).add(middleCenter)
    }

    override fun init() {
        super.init()

        val content = LayoutFactory.vertical(alignment = MIDDLE) {
            TextReplacementManager.categories.forEachIndexed { index, categories ->
                createEntry(categories, width / 5, SPACER * 3).add()
                if (index + 1 < HotkeyManager.categories.size) {
                    createSeparator(width / 5 - SPACER * 2).add {
                        alignHorizontallyCenter()
                    }
                }
            }
        }

        val modalWidth = content.width + PADDING * 2

        this.layout = Layouts.column()
            .withGap(PADDING)
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, HEADER_HEIGHT + PADDING * 2)
                    .withTexture(id("hotkey/header"))
                    .withContents { contents: FrameLayout ->
                        contents.addChild(
                            LayoutFactory.frame(modalWidth - PADDING * 2, HEADER_HEIGHT + PADDING * 2) {
                                Widgets.text("Move Hotkey").withColor(CatppuccinColors.Mocha.lavenderColor).add(middleLeft)
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
            .withChildren(
                content.withPadding(PADDING, top = 0).asScrollable(
                    modalWidth,
                    min(content.height + PADDING * 2, height - height / 6),
                ).apply {
                    withScrollY(lastScrollGetter())
                    lastScrollGetter = { yScroll }
                },
            )
            .build { widget: AbstractWidget -> this.addRenderableWidget(widget) }

        FrameLayout.centerInRectangle(this.layout, this.rectangle)
    }

    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick)
        this.extractTransparentBackground(graphics)

        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            id("hotkey/background"),
            this.layout.x, this.layout.y,
            this.layout.width, this.layout.height,
        )
    }
}
