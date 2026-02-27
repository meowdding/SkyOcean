package me.owdding.skyocean.features.hotkeys

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.string.MultilineTextWidget
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.ui.UITexts
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.features.hotkeys.ConditionalHotkeyScreen.SPACER
import me.owdding.skyocean.features.hotkeys.ConditionalHotkeyScreen.currentMainScroll
import me.owdding.skyocean.features.hotkeys.ConditionalHotkeyScreen.headerSprite
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.extensions.asScrollable
import me.owdding.skyocean.utils.extensions.asScrollableWidget
import me.owdding.skyocean.utils.extensions.createButton
import me.owdding.skyocean.utils.extensions.createSeparator
import me.owdding.skyocean.utils.extensions.createText
import me.owdding.skyocean.utils.extensions.framed
import me.owdding.skyocean.utils.extensions.middleLeft
import me.owdding.skyocean.utils.extensions.middleRight
import me.owdding.skyocean.utils.extensions.withPadding
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderPipelines
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.min

private const val PADDING = 5
private const val HEADER_HEIGHT = PADDING * 2

class HotkeyPresetsScreen(
    parent: Screen? = McScreen.self,
) : Overlay(parent), IgnoreHotkeyInputs {
    var lastScrollGetter = { 0 }

    private var layout: Layout = LayoutFactory.empty()

    fun createEntry(preset: HotkeyPresets.HotkeyPreset, width: Int, height: Int) = LayoutFactory.frame(width, height) {
        LayoutFactory.vertical {
            createText(preset.name) {
                color = CatppuccinColors.Mocha.sky
            }.withPadding(bottom = 2).add()
            MultilineTextWidget(
                preset.description.asComponent(),
                width - width / 4,
            ).setColor(CatppuccinColors.Mocha.text)
                .withPadding(2)
                .add()
        }.withPadding(left = SPACER).add(middleLeft)

        createButton(
            text = Text.of("Add"),
            texture = headerSprite,
            color = CatppuccinColors.Mocha.lavenderColor,
            width = 30,
            height = PADDING * 3,
        ).withPadding(4).add(middleRight)
    }

    override fun init() {
        super.init()

        val content = LayoutFactory.vertical {
            HotkeyPresets.presets.forEach { preset ->
                createEntry(preset, width / 3, SPACER * 7).add()
                createSeparator(width / 3 - SPACER * 2).add {
                    alignHorizontallyCenter()
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
                                Widgets.text("Import Hotkey Preset").withColor(CatppuccinColors.Mocha.lavenderColor).add(middleLeft)
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
