//? > 1.21.8 {
package me.owdding.skyocean.features.hotkeys

import com.teamresourceful.resourcefullib.common.color.Color
import earth.terrarium.olympus.client.ui.UIIcons
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.RIGHT
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.hotkeys.system.Hotkey
import me.owdding.skyocean.features.hotkeys.system.HotkeyCategory
import me.owdding.skyocean.features.hotkeys.system.HotkeyManager
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.components.CatppuccinColors
import me.owdding.skyocean.utils.extensions.asScrollableWidget
import me.owdding.skyocean.utils.extensions.asWidget
import me.owdding.skyocean.utils.extensions.bottomCenter
import me.owdding.skyocean.utils.extensions.createButton
import me.owdding.skyocean.utils.extensions.createSeparator
import me.owdding.skyocean.utils.extensions.createText
import me.owdding.skyocean.utils.extensions.framed
import me.owdding.skyocean.utils.extensions.middleCenter
import me.owdding.skyocean.utils.extensions.middleLeft
import me.owdding.skyocean.utils.extensions.middleRight
import me.owdding.skyocean.utils.extensions.setScreen
import me.owdding.skyocean.utils.extensions.string
import me.owdding.skyocean.utils.extensions.topCenter
import me.owdding.skyocean.utils.extensions.withPadding
import me.owdding.skyocean.utils.extensions.withTexturedBackground
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.CommonComponents
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined
import kotlin.math.max

/**
 * todo for next release
 *  - implement KeyMapping action
 *  - Maybe add rconfig toggle action?
 *  - Condition builder string representation
 *  - Held item condition (probably with extra item conditions??)
 */
object IslandSpecificHotkeyScreen : SkyOceanScreen("Island Specific Keybinds"), IgnoreHotkeyInputs {

    private var tryDeleting: Hotkey? = null

    private val unhovered = CatppuccinColors.Mocha.subtext0Color
    private val hovered = CatppuccinColors.Mocha.lavenderColor
    private val disabled = Text.of("Disabled") {
        this.color = CatppuccinColors.Mocha.red
    }
    private val enabled = Text.of("Enabled") {
        this.color = CatppuccinColors.Mocha.green
    }
    val headerSprite = WidgetSprites(
        SkyOcean.id("hotkey/header"),
        SkyOcean.id("hotkey/header_hovered"),
    )
    var toggleWidth = 0

    var currentCategoryScroll = { 0 }
    var currentMainScroll = { 0 }
    const val SPACER = 5

    var currentCategory = HotkeyManager.defaultCategory
        set(value) {
            field = value
            tryDeleting = null
        }

    fun createLeftPanel(sliceWidth: Int, height: Int): LayoutElement {
        val header = LayoutFactory.frame(sliceWidth, SPACER * 4) {
            string("Categories", CatppuccinColors.Mocha.text, middleCenter)
        }.withTexturedBackground("hotkey/header")

        val categories = LayoutFactory.frame(height = height - header.height - SPACER) {
            LayoutFactory.vertical {
                widget(createCategory(HotkeyManager.defaultCategory, sliceWidth, 15))
                HotkeyManager.categories.forEach { category ->
                    createCategory(category, sliceWidth, 15).add()
                }

                LayoutFactory.horizontal(SPACER * 2) {
                    createButton(
                        texture = null,
                        icon = UIIcons.PLUS,
                        color = unhovered,
                        hoveredColor = hovered,
                        hover = Text.of("Add category", CatppuccinColors.Mocha.text),
                        leftClick = setScreen {
                            EditCategoryModal(this@IslandSpecificHotkeyScreen, sliceWidth) { name, madeBy ->
                                currentCategory = HotkeyManager.createCategory(name, madeBy)
                            }
                        },
                    ).add()
                    createButton(
                        texture = null,
                        icon = UIIcons.DOWNLOAD,
                        color = unhovered,
                        hoveredColor = hovered,
                        hover = Text.of("Import category", CatppuccinColors.Mocha.text),
                        leftClick = setScreen {
                            val data = HotkeyUtils.readData(McClient.clipboard)
                            data.exceptionOrNull()?.let {
                                it.printStackTrace()
                                return@setScreen ShowMessageModal(
                                    titleComponent = "An error occurred".asComponent {
                                        color = CatppuccinColors.Mocha.red
                                    },
                                    message = "Failed to import data!\n".asComponent {
                                        color = CatppuccinColors.Mocha.red
                                        append("See the logs for more details!", CatppuccinColors.Mocha.text)
                                    },
                                )
                            }

                            val result = data.getOrThrow()
                            val category = HotkeyManager.createCategory(result.category.name, result.category.username)

                            result.hotkeys.forEach {
                                HotkeyManager.register(it.copy(group = category.identifier))
                            }
                            currentCategory = category

                            ShowMessageModal(
                                titleComponent = "Imported Hotkeys!".asComponent {
                                    color = CatppuccinColors.Mocha.green
                                },
                                message = Text.multiline(
                                    Text.of("Successfully imported data!", CatppuccinColors.Mocha.green),
                                    CommonComponents.EMPTY,
                                    Text.of("Imported Data"),
                                    Text.of("Name: ") {
                                        append(result.category.name, CatppuccinColors.Mocha.sky)
                                    },
                                    Text.of("Made by: ") {
                                        append(result.category.username, CatppuccinColors.Mocha.green)
                                    },
                                    Text.of("Hotkeys: ${result.hotkeys.size}"),
                                ) {
                                    color = CatppuccinColors.Mocha.text
                                },
                            )
                        },
                    ).add()
                }.withPadding(bottom = SPACER, top = SPACER).add { alignHorizontallyCenter() }
            }.add(topCenter)
        }

        return LayoutFactory.frame(sliceWidth, height) {
            header.add {
                alignVerticallyTop()
            }

            categories.asScrollableWidget(
                width = sliceWidth,
                height = height - header.height - SPACER,
                alwaysShowScrollBar = true,
            ).apply {
                withScrollY(currentCategoryScroll())
                currentCategoryScroll = { yScroll }
            }.withPadding(
                bottom = SPACER,
            ).add(bottomCenter)
        }
    }

    fun createHeader(sliceWidth: Int, panelWidth: Int, entry: List<Hotkey>) = LayoutFactory.frame(panelWidth, SPACER * 6) {
        LayoutFactory.vertical {
            createText(currentCategory.name, CatppuccinColors.Mocha.sky) {
                append(" Keybinds") {
                    color = CatppuccinColors.Mocha.text
                }
            }.withPadding(left = SPACER).add()
            if (currentCategory === HotkeyManager.defaultCategory) return@vertical
            createText("Made by ", CatppuccinColors.Mocha.text) {
                append(currentCategory.username) {
                    color = CatppuccinColors.Mocha.green
                }
            }.withPadding(left = SPACER).add()
        }.add(middleLeft)
        LayoutFactory.horizontal {
            if (currentCategory !== HotkeyManager.defaultCategory) {
                createButton(
                    texture = null,
                    icon = UIIcons.TRASH,
                    color = unhovered,
                    hoveredColor = hovered,
                    hover = Text.of("Delete category", CatppuccinColors.Mocha.text),
                    leftClick = setScreen {
                        DeleteCategoryModal(this@IslandSpecificHotkeyScreen, sliceWidth, currentCategory) {
                            currentCategory = HotkeyManager.defaultCategory
                        }
                    },
                ).withPadding(right = SPACER).add()
                createButton(
                    texture = null,
                    icon = UIIcons.PENCIL,
                    color = unhovered,
                    hoveredColor = hovered,
                    hover = Text.of("Edit category", CatppuccinColors.Mocha.text),
                    leftClick = setScreen {
                        EditCategoryModal(this@IslandSpecificHotkeyScreen, sliceWidth, currentCategory) { name, username ->
                            currentCategory.username = username
                            currentCategory.name = name
                        }
                    },
                ).withPadding(right = SPACER).add()
            }
            createButton(
                texture = null,
                icon = UIIcons.SAVE,
                color = unhovered,
                hoveredColor = hovered,
                hover = Text.of("Export category", CatppuccinColors.Mocha.text),
                leftClick = setScreen {
                    val data = HotkeyUtils.writeData(currentCategory)
                    data.exceptionOrNull()?.let {
                        it.printStackTrace()
                        return@setScreen ShowMessageModal(
                            titleComponent = "An error occurred".asComponent {
                                color = CatppuccinColors.Mocha.red
                            },
                            message = "Failed to export data!\n".asComponent {
                                color = CatppuccinColors.Mocha.red
                                append("See the logs for more details!", CatppuccinColors.Mocha.text)
                            },
                        )
                    }

                    val result = data.getOrThrow()

                    McClient.clipboard = result
                    ShowMessageModal(
                        titleComponent = "Exported data".asComponent(),
                        message = Text.multiline(
                            Text.of("Successfully exported data!", CatppuccinColors.Mocha.green),
                            CommonComponents.EMPTY,
                            Text.of("Exported 1 Category containing ${entry.size} Hotkey(s)!"),
                        ) {
                            color = CatppuccinColors.Mocha.text
                        },
                    )
                },
            ).withPadding(right = SPACER).add()
        }.add(middleRight)
    }.withTexturedBackground("hotkey/header")

    fun createRightPanel(sliceWidth: Int, height: Int): LayoutElement {
        val panelWidth = sliceWidth * 2 + SPACER
        val entry = getHotkeysInCategory().sortedBy { it.timeCreated }
        val header = createHeader(sliceWidth, panelWidth, entry)

        val mainSectionHeight = height - header.height - SPACER
        val mainSection = LayoutFactory.vertical {
            entry.forEach { hotkey ->
                createEntry(hotkey, panelWidth, SPACER * 7).add()
                createSeparator(panelWidth - SPACER * 2).add {
                    alignHorizontallyCenter()
                }
            }

            val text = Text.of("Create New")
            createButton(
                texture = null,
                text = text,
                icon = UIIcons.PLUS,
                color = unhovered,
                hoveredColor = hovered,
                leftClick = setScreen {
                    EditHotkeyModal(
                        this@IslandSpecificHotkeyScreen,
                        null,
                    ) { keybind, action, condition, name, enabled ->
                        HotkeyManager.register(
                            Hotkey(
                                keybind,
                                action,
                                condition,
                                name,
                                enabled,
                                currentCategory.takeUnless { it.isDefault() }?.identifier,
                                System.currentTimeMillis(),
                            ),
                        )
                    }
                },
                width = 12 + SPACER + McFont.width(text),
            ).withPadding(SPACER).add {
                alignHorizontallyCenter()
            }
        }.framed(height = mainSectionHeight) {
            alignVerticallyTop()
        }.asScrollableWidget(
            panelWidth,
            mainSectionHeight,
            alwaysShowScrollBar = true,
        ).apply {
            withScrollY(currentMainScroll())
            currentMainScroll = { yScroll }
        }.withPadding(bottom = SPACER)

        return LayoutFactory.frame(panelWidth, height) {
            header.add(topCenter)
            mainSection.add(bottomCenter)
        }
    }

    fun getHotkeysInCategory(): Collection<Hotkey> = this.currentCategory.getHotkeysInCategory()

    override fun init() {
        toggleWidth = max(McFont.width(disabled), McFont.width(enabled))
        val height = height - height / 3
        val sliceWidth = width / 6

        val leftPanel = createLeftPanel(sliceWidth, height)
        val rightPanel = createRightPanel(sliceWidth, height)

        LayoutFactory.frame {
            vertical(SPACER) {
                LayoutFactory.frame {
                    spacer(sliceWidth * 3 + SPACER * 2, SPACER * 4)
                    Text.of {
                        append(ChatUtils.prefix)
                    }.asWidget().withPadding(left = SPACER).add(middleCenter)
                }.withTexturedBackground("hotkey/background").add()
                horizontal(SPACER) {
                    leftPanel.withTexturedBackground("hotkey/background").add()
                    rightPanel.withTexturedBackground("hotkey/background").add()
                }
            }
        }.center().applyLayout()

        super.init()
    }

    override fun onClose() {
        currentCategoryScroll = { 0 }
        currentMainScroll = { 0 }
        currentCategory = HotkeyManager.defaultCategory
        super.onClose()
    }

    private fun createCategory(category: HotkeyCategory, width: Int, height: Int): LayoutElement = LayoutFactory.frame(width, height) {
        val isSelected = category == currentCategory
        val color: Color = if (isSelected) hovered else unhovered
        spacer(width, height)
        createButton(
            texture = null,
            text = Text.of(category.name) {
                underlined = isSelected
            },
            color = color,
            hoveredColor = hovered,
            width = width,
            height = height,
            click = withRebuild {
                currentCategory = category
            },
        ).add(middleCenter)
    }

    private fun withRebuild(resetDelete: Boolean = true, action: () -> Unit): () -> Unit = {
        if (resetDelete) this.tryDeleting = null
        action()
        rebuildWidgets()
    }

    private fun createEntry(hotkey: Hotkey, width: Int, height: Int): LayoutElement = LayoutFactory.frame(width, height) {
        val keyComponent = hotkey.formatKeys()
        LayoutFactory.vertical {
            createText(hotkey.name) {
                color = CatppuccinColors.Mocha.text
            }.withPadding(bottom = 2).add()
            createText(keyComponent).withPadding(4).withTexturedBackground("hotkey/header").add()
        }.withPadding(left = SPACER).add(middleLeft)
        LayoutFactory.vertical(alignment = RIGHT, spacing = 1) {
            createButton(
                texture = headerSprite,
                text = if (hotkey.enabled) enabled else disabled,
                width = toggleWidth + SPACER * 2,
                height = 15,
                click = withRebuild {
                    hotkey.enabled = !hotkey.enabled
                },
            ).add()
            horizontal(1) {
                val edit = Text.of("Edit")
                createButton(
                    texture = headerSprite,
                    text = edit,
                    width = McFont.width(edit) + SPACER * 2,
                    color = unhovered,
                    height = 15,
                    click = setScreen {
                        EditHotkeyModal(this@IslandSpecificHotkeyScreen, hotkey) { keybind, action, condition, name, enabled ->
                            hotkey.keybind = keybind
                            hotkey.action = action
                            hotkey.condition = condition
                            hotkey.name = name
                            hotkey.enabled = enabled
                        }
                    },
                ).add()


                val instantDelete = tryDeleting === hotkey
                val text = if (instantDelete) {
                    Text.of("Confirm?", CatppuccinColors.Mocha.red)
                } else {
                    Text.of("Delete", CatppuccinColors.Mocha.red)
                }
                createButton(
                    texture = headerSprite,
                    text = text,
                    width = McFont.width(text) + SPACER * 2,
                    height = 15,
                    click = withRebuild(resetDelete = false) {
                        if (instantDelete) {
                            HotkeyManager.unregister(hotkey)
                            tryDeleting = null
                        } else {
                            tryDeleting = hotkey
                        }
                    },
                ).add()
            }
        }.withPadding(right = SPACER * 2).add(middleRight)

    }

}
//?}
