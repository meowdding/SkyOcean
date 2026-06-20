package me.owdding.skyocean.features.text

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.ui.UITexts
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.layouts.asWidget
import me.owdding.lib.layouts.withPadding
import me.owdding.lib.rendering.text.textShader
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.features.hotkeys.IgnoreHotkeyInputs
import me.owdding.skyocean.features.hotkeys.ShowMessageModal
import me.owdding.skyocean.features.item.custom.ui.standard.HEADER_HEIGHT
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.chat.OceanGradients
import me.owdding.skyocean.utils.components.TagComponentSerialization
import me.owdding.skyocean.utils.extensions.*
import me.owdding.skyocean.utils.rendering.ExtraWidgetRenderers
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined

class EditReplacementModal(
    val parent: Screen?,
    val textReplacement: TextReplacement?,
    maxPriority: Int? = null,
    val callback: (key: String, value: Component, priority: Int, wholeWord: Boolean, ignoreCase: Boolean, enabled: Boolean) -> Unit,
) : Overlay(parent), IgnoreHotkeyInputs, DisableReplacements {


    private var layout: Layout = LayoutFactory.empty()

    private var key = ListenableState.of(textReplacement?.key ?: "")
    private var preview: Component = textReplacement?.value ?: CommonComponents.EMPTY
    private var value = ListenableState.of(TagComponentSerialization.serialize(preview)).apply {
        registerListener {
            preview = TagComponentSerialization.deserialize(it)
        }
    }

    private val enabled = ListenableState.of(textReplacement?.enabled ?: true)
    private val priority = ListenableState.of(textReplacement?.priority ?: maxPriority?.plus(1) ?: 0)
    private val wholeWord = ListenableState.of(textReplacement?.wholeWord ?: false)
    private val ignoreCase = ListenableState.of(textReplacement?.ignoreCase ?: true)

    fun content(width: Int): Layout {

        return LayoutFactory.vertical(PADDING) {
            LayoutFactory.frame(width - PADDING * 2) {
                LayoutFactory.vertical {
                    vertical {
                        createText("Enabled", CatppuccinColors.Mocha.text).withPadding(1).add(bottomLeft)
                        createToggleButton(
                            enabled,
                            width = width / 2 - PADDING * 2,
                            height = 20,
                            onClick = ::rebuildWidgets,
                        ).add()
                    }
                }.add(middleLeft)
                LayoutFactory.vertical {
                    vertical {
                        createText("Priority", CatppuccinColors.Mocha.text).withPadding(1).add(bottomLeft)
                        createIntInput(
                            state = priority,
                            texture = id("text_replacements/inset"),
                            height = 20,
                            width = width / 2 - PADDING * 2,
                        ).add()
                    }
                }.add(middleRight)
            }.add(middleCenter)

            LayoutFactory.frame(width - PADDING * 2) {
                LayoutFactory.vertical {
                    vertical {
                        createText("Original", CatppuccinColors.Mocha.text).withPadding(1).add(bottomLeft)
                        createTextInput(
                            key,
                            placeholder = "Original",
                            texture = id("text_replacements/inset"),
                            width = width / 2 - PADDING * 2
                        ).add()
                    }
                }.add(middleLeft)
                LayoutFactory.frame(width / 2 - PADDING * 2) {
                    LayoutFactory.vertical {
                        vertical {
                            horizontal {
                                createText("Whole Words", CatppuccinColors.Mocha.text).withPadding(1).add(middleLeft)
                                Displays.sprite(id("info"), 7, 7).withTooltip {
                                    add("Whole Words means that replacements will only replace whole words.")
                                    space()
                                    add {
                                        this.color = CatppuccinColors.Mocha.text
                                        append("If you have a replacement for '")
                                        append("bal") { this.color = CatppuccinColors.Mocha.red }
                                        append("', it will only get replaced if it is surrounded by spaces.")
                                    }
                                    add {
                                        this.color = CatppuccinColors.Mocha.subtext1
                                        append(" The '")
                                        append("bal") { this.color = CatppuccinColors.Mocha.red }
                                        append("' in '")
                                        append("bal") { this.color = CatppuccinColors.Mocha.red }
                                        append("loon") { this.color = CatppuccinColors.Mocha.green }
                                        append("' will not get replaced.")
                                    }
                                }.add()
                            }
                            createToggleButton(
                                wholeWord,
                                width = width / 4 - PADDING * 2,
                                height = 20,
                                onClick = ::rebuildWidgets,
                            ).add()
                        }
                    }.add(middleLeft)
                    LayoutFactory.vertical {
                        vertical {
                            createText("Ignore Case", CatppuccinColors.Mocha.text).withPadding(1).add(middleLeft)
                            createToggleButton(
                                ignoreCase,
                                width = width / 4 - PADDING * 2,
                                height = 20,
                                onClick = ::rebuildWidgets,
                            ).add()
                        }
                    }.add(middleRight)
                }.add(middleRight)
            }.add(middleCenter)
            horizontal {
                createText("Replacement", CatppuccinColors.Mocha.text).withPadding(1).add(middleLeft)
                Displays.sprite(id("info"), 7, 7).withTooltip {
                    add("The text field below supports a some formatting tags!")
                    space()
                    add("The basic formatting tags include the following")
                    add {
                        append(" • ")
                        append("<bold>") { this.bold = true }
                        append(", ")
                        append("<italic>") { this.italic = true }
                        append(", ")
                        append("<strikethrough>") { this.strikethrough = true }
                        append(", ")
                        append("<underlined>") { this.underlined = true }
                        append(" and <obfuscated>")
                    }
                    ChatFormatting.entries.filter { it.isColor }.map {
                        text("<${it.serializedName}>") {
                            this.color = it.color!!
                        }
                    }.chunked(5).forEach {
                        add {
                            append(" • ")
                            append(Text.join(it, separator = text(", ")))
                        }
                    }
                    add(" • ") {
                        append("<color #f38ba8>") {
                            this.color = OceanColors.PINK
                        }
                    }
                    space()
                    add("The \"complex\" style tags include the following")
                    OceanGradients.entries.filterNot { it.isDisabled }.map {
                        text("<${it.name.lowercase()}>") {
                            this.textShader = it
                        }
                    }.chunked(5).forEach {
                        add {
                            append(" • ")
                            append(Text.join(it, separator = text(", ")))
                        }
                    }
                    add {
                        append(" • ")
                        append("<gradient ")
                        append("#color1 ") { this.color = TextColor.BLUE }
                        append("#color2 ") { this.color = TextColor.GREEN }
                        append("... ") { this.color = TextColor.GRAY }
                        append("#colorN ") { this.color = TextColor.MAGENTA }
                        append("#color1") { this.color = TextColor.BLUE }
                        append(">")
                    }
                    space()
                    add("Note! To get a gradient that loops perfectly you\n must include the start color at the end again!") {
                        this.color = TextColor.YELLOW
                    }
                }.add()
            }
            createTextInput(
                value,
                placeholder = "Replacement",
                texture = id("text_replacements/inset"),
                width = width - PADDING * 2
            ).add()
            Widgets.button()
                .withSize(width - PADDING * 2, 10)
                .withTexture(null)
                .withRenderer(ExtraWidgetRenderers.supplied { WidgetRenderers.text(preview) })
                .asDisabled()
                .add(middleLeft)
        }
    }

    override fun init() {
        super.init()

        val minWidth = width / 6 * 2
        val content = content(minWidth)
        val modalWidth = content.width + PADDING * 2

        this.layout = Layouts.column()
            .withGap(PADDING)
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, HEADER_HEIGHT + PADDING * 2)
                    .withTexture(id("text_replacements/header"))
                    .withContents { contents: FrameLayout ->
                        contents.addChild(
                            LayoutFactory.frame(modalWidth - PADDING * 2, HEADER_HEIGHT + PADDING * 2) {
                                Widgets.text(
                                    Text.of("${if (textReplacement == null) "Create" else "Edit"} Text Replacement"),
                                ).withColor(CatppuccinColors.Mocha.lavenderColor).add(middleLeft)
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
                content.withPadding(PADDING, top = 0)
            )
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, PADDING * 6)
                    .withTexture(id("text_replacements/header"))
                    .withContents { contents: FrameLayout ->
                        contents.addChild(
                            createButton(
                                text = Text.of("Cancel", CatppuccinColors.Mocha.base),
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
                                    fun require(type: String) = McClient.setScreenAsync {
                                        ShowMessageModal(
                                            "Missing Value".asComponent { color = CatppuccinColors.Mocha.red },
                                            message = Text.of {
                                                append("Please add a ")
                                                append(type, CatppuccinColors.Mocha.sky)
                                                append(" to proceed!")
                                                color = CatppuccinColors.Mocha.text
                                            },
                                        )
                                    }

                                    callback(
                                        key.get().takeUnless { it.isEmpty() } ?: run {
                                            require("Key")
                                            return@createButton
                                        },
                                        preview.takeUnless { it.stripped.isEmpty() } ?: run {
                                            require("Value")
                                            return@createButton
                                        },
                                        priority.get(),
                                        wholeWord.get(),
                                        ignoreCase.get(),
                                        enabled.get(),
                                    )
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



    //~ if >= 26.1 'render' -> 'extract' {
    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick)
        this.extractTransparentBackground(graphics)
        //~ }

        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            id("text_replacements/background"),
            this.layout.x, this.layout.y,
            this.layout.width, this.layout.height,
        )
    }
}
