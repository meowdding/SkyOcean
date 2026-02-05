package me.owdding.skyocean.features.hotkeys

import com.mojang.blaze3d.platform.InputConstants
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.ui.UITexts
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.layouts.asWidget
import me.owdding.lib.layouts.withPadding
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.features.hotkeys.actions.HotkeyAction
import me.owdding.skyocean.features.hotkeys.actions.HotkeyActionType
import me.owdding.skyocean.features.hotkeys.conditions.HotkeyCondition
import me.owdding.skyocean.features.hotkeys.system.ConflictContext
import me.owdding.skyocean.features.hotkeys.system.Hotkey
import me.owdding.skyocean.features.hotkeys.system.Keybind
import me.owdding.skyocean.features.hotkeys.system.KeybindSettings
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.utils.components.CatppuccinColors
import me.owdding.skyocean.utils.extensions.asScrollable
import me.owdding.skyocean.utils.extensions.bottomLeft
import me.owdding.skyocean.utils.extensions.createButton
import me.owdding.skyocean.utils.extensions.createIntInput
import me.owdding.skyocean.utils.extensions.createSprite
import me.owdding.skyocean.utils.extensions.createText
import me.owdding.skyocean.utils.extensions.createTextInput
import me.owdding.skyocean.utils.extensions.createToggleButton
import me.owdding.skyocean.utils.extensions.middleCenter
import me.owdding.skyocean.utils.extensions.middleLeft
import me.owdding.skyocean.utils.extensions.middleRight
import me.owdding.skyocean.utils.extensions.setScreen
import me.owdding.skyocean.utils.extensions.topLeft
import me.owdding.skyocean.utils.extensions.topRight
import me.owdding.skyocean.utils.extensions.withPadding
import me.owdding.skyocean.utils.extensions.withTexturedBackground
import me.owdding.skyocean.utils.rendering.ExtraWidgetRenderers
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.contracts.contract
import kotlin.math.min

private const val PADDING = 5
private const val HEADER_HEIGHT = PADDING * 2

class EditHotkeyModal(
    val parent: Screen?,
    val hotkey: Hotkey?,
    val callback: (keybind: Keybind, action: HotkeyAction, condition: HotkeyCondition, name: String, enabled: Boolean) -> Unit,
) : Overlay(parent), IgnoreHotkeyInputs {

    var recordKeys = false

    private var layout: Layout = LayoutFactory.empty()

    private val name = ListenableState.of(hotkey?.name ?: "New Hotkey")
    private val keybind = hotkey?.keybind
    private var action = hotkey?.action
    private var condition = hotkey?.condition

    private val keys = keybind?.keys?.toMutableList() ?: mutableListOf()

    private val enabled = ListenableState.of(hotkey?.enabled ?: true)
    private val orderSensitive = ListenableState.of(keybind?.settings?.orderSensitive ?: false)
    private val allowExtraKeys = ListenableState.of(keybind?.settings?.allowExtraKeys ?: false)

    private val priority = ListenableState.of(keybind?.settings?.priority ?: 0)
    private val context: ListenableState<ConflictContext> = ListenableState.of(keybind?.settings?.context ?: ConflictContext.GLOBAL)

    var lastScrollGetter = { 0 }

    private fun withRebuild(action: () -> Unit): () -> Unit = {
        action()
        rebuildWidgets()
    }

    override fun init() {
        super.init()

        val minWidth = width / 6 * 2
        val widgetContext = WidgetContext(minWidth - PADDING * 2, rebuildCallback = ::rebuildWidgets)
        val conditionLayout = LayoutFactory.frame(minWidth - PADDING * 2) {
            try {
                widgetContext.createEntry(condition) {
                    condition = it
                    rebuildWidgets()
                }.add(middleCenter)
            } catch (_: UnsupportedOperationException) {
                LayoutFactory.frame(widgetContext.width) {
                    createText("Can't display condition!", CatppuccinColors.Mocha.surface0).add(middleCenter)
                }.withTexturedBackground(widgetContext.background)
            }
            widgetContext.advance()
        }

        widgetContext.reset()
        val actionLayout = LayoutFactory.frame(minWidth - PADDING * 2) {
            val action = action
            val state = ListenableState.of(HotkeyActionType.NONE)
            val dropdownState = DropdownState(null, state, false)
            val dropdown = widgetContext.createActionDropdown(dropdownState)
            val callback: (HotkeyAction) -> Unit = {
                this@EditHotkeyModal.action = it
                rebuildWidgets()
            }
            if (action == null) {
                state.registerListener {
                    callback(it.builder!!.invoke())
                }
                val dropdown = dropdown
                LayoutFactory.frame(widgetContext.width) {
                    dropdown.add(middleLeft)
                }.withTexturedBackground(widgetContext.background).add()
            } else {
                context(widgetContext) {
                    action.toWidget(callback).withTexturedBackground(widgetContext.background).add()
                }
            }
            widgetContext.advance()
        }


        val content = LayoutFactory.vertical(PADDING) {
            LayoutFactory.frame(conditionLayout.width - PADDING * 2) {
                LayoutFactory.vertical(PADDING) {
                    vertical {
                        createText("Name", CatppuccinColors.Mocha.text).withPadding(1).add()
                        createTextInput(
                            state = name,
                            placeholder = "Name",
                            texture = id("hotkey/inset"),
                            width = conditionLayout.width / 2 - PADDING * 2,
                        ).add()
                    }
                    vertical {
                        createText("Context", CatppuccinColors.Mocha.text).withPadding(1).add()
                        createButton(
                            text = Text.of(context.get().name.toTitleCase(), CatppuccinColors.Mocha.text),
                            texture = createSprite(id("hotkey/inset")),
                            width = conditionLayout.width / 2 - PADDING * 2,
                            height = 20,
                            click = withRebuild {
                                context.set(context.get().next())
                            },
                        ).add()
                    }
                }.add(topLeft)

                LayoutFactory.vertical(PADDING) {
                    vertical {
                        createText("Keys", CatppuccinColors.Mocha.text).withPadding(1).add(bottomLeft)
                        createButton(
                            texture = createSprite(id("hotkey/inset")),
                            width = conditionLayout.width / 2 - PADDING * 2,
                            height = 20,
                            click = {
                                keys.clear()
                                recordKeys = true
                            },
                        ) {
                            val left = Text.of("< ", CatppuccinColors.Mocha.yellow)
                            val right = Text.of(" >", CatppuccinColors.Mocha.yellow)

                            withRenderer(
                                ExtraWidgetRenderers.supplied {
                                    WidgetRenderers.text(
                                        (if (keys.isNotEmpty()) Hotkey.formatKeys(keys, orderSensitive.get()) else Text.of(
                                            "UNBOUND",
                                            CatppuccinColors.Mocha.red,
                                        )).let {
                                            if (recordKeys) Text.join(left, it, right) else it
                                        },
                                    )
                                },
                            )
                        }.add()
                    }
                    vertical {
                        createText("Priority", CatppuccinColors.Mocha.text).withPadding(1).add(bottomLeft)
                        createIntInput(
                            state = priority,
                            texture = id("hotkey/inset"),
                            width = conditionLayout.width / 2 - PADDING * 2,
                            height = 20,
                        ).add()
                    }
                }.add(topRight)
            }.add(middleCenter)
            LayoutFactory.frame(conditionLayout.width - PADDING * 2) {
                LayoutFactory.vertical {
                    vertical {
                        createText("Enabled", CatppuccinColors.Mocha.text).withPadding(1).add(bottomLeft)

                        createToggleButton(
                            enabled,
                            width = conditionLayout.width / 3 - PADDING * 3,
                            onClick = ::rebuildWidgets,
                        ).add()
                    }
                }.add(middleLeft)
                LayoutFactory.vertical {
                    vertical {
                        createText("Allow Extra Keys", CatppuccinColors.Mocha.text).withPadding(1).add(bottomLeft)
                        createToggleButton(
                            allowExtraKeys,
                            trueText = "Yes",
                            falseText = "No",
                            width = conditionLayout.width / 3 - PADDING * 3,
                            onClick = ::rebuildWidgets,
                        ).add()
                    }
                }.add(middleCenter)

                LayoutFactory.vertical {
                    vertical {
                        createText("Order Sensitive", CatppuccinColors.Mocha.text).withPadding(1).add(bottomLeft)
                        createToggleButton(
                            orderSensitive,
                            trueText = "Yes",
                            falseText = "No",
                            width = conditionLayout.width / 3 - PADDING * 3,
                            onClick = ::rebuildWidgets,
                        ).add()
                    }
                }.add(middleRight)
            }.add(middleCenter)

            createText("Condition", CatppuccinColors.Mocha.text).withPadding(PADDING, bottom = 0).add()
            conditionLayout.withPadding(left = PADDING, right = PADDING).add(middleCenter)
            createText("Action", CatppuccinColors.Mocha.text).withPadding(PADDING, bottom = 0).add()
            actionLayout.withPadding(left = PADDING, right = PADDING).add(middleCenter)
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
                                Widgets.text(
                                    Text.of("${if (hotkey == null) "Create" else "Edit"} Hotkey")
                                ).withColor(CatppuccinColors.Mocha.lavenderColor).add(middleLeft)
                                createButton(
                                    texture = null,
                                    icon = UIIcons.X,
                                    click = ::onClose,
                                    color = CatppuccinColors.Mocha.lavenderColor,
                                    hover = UITexts.BACK,
                                ).add(middleRight)
                            }.asWidget().withPadding(PADDING, bottom = 2, top = 0)
                        )
                    }
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
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, PADDING * 6)
                    .withTexture(id("hotkey/header"))
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
                                                append("Please add a${if (type == "Action") "n" else ""} ")
                                                append(type, CatppuccinColors.Mocha.sky)
                                                append(" to proceed!")
                                                color = CatppuccinColors.Mocha.text
                                            }
                                        )
                                    }

                                    callback(
                                        Keybind(
                                            keys = keys,
                                            settings = KeybindSettings(
                                                orderSensitive = orderSensitive.get(),
                                                allowExtraKeys = allowExtraKeys.get(),
                                                priority = priority.get(),
                                                context = context.get(),
                                            ),
                                        ),
                                        action ?: run {
                                            require("Action")
                                            return@createButton
                                        },
                                        condition ?: run {
                                            require("Condition")
                                            return@createButton
                                        },
                                        name.get(),
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

    override fun mouseClicked(event: MouseButtonEvent?, isDoubleClick: Boolean): Boolean {
        this.recordKeys = false
        return super.mouseClicked(event, isDoubleClick)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (recordKeys) {
            if (event.key == InputConstants.KEY_ESCAPE) {
                this.recordKeys = false
                return true
            }
            val key = InputConstants.getKey(event)
            if (this.orderSensitive.get()) {
                this.keys.add(key)
            } else if (!this.keys.contains(key)) {
                this.keys.add(key)
            }
        }
        return super.keyPressed(event)
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
