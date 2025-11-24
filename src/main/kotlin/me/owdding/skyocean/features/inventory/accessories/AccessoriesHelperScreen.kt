package me.owdding.skyocean.features.inventory.accessories

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.textbox.TextBox
import earth.terrarium.olympus.client.ui.context.ContextMenu
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.builder.LEFT
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.builder.RIGHT
import me.owdding.lib.displays.*
import me.owdding.lib.displays.Displays.background
import me.owdding.lib.extensions.rightPad
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.asWidgetTable
import me.owdding.skyocean.utils.extensions.asScrollable
import me.owdding.skyocean.utils.extensions.withPadding
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

/*
 * TODO:
 *  - Marked accessories (they show on top of the list, and also an icon or smth)
 *  - CraftHelper support
 *  - Remove Rift-exclusive accessories
 *  - Be able to select a specific accessory in cycling accessories
 *  - Add sort options
 *      - MP
 *      - Rarity
 *      - Price (take into account previous tier if upgrading)
 *      - Price per MP (take into account previous tier if upgrading)
 *  - Highlight accessories you have materials for
 *  - Handle accessories that upgrade with rarity (eg. pandora's box, pulse ring, etc)
 *
 * TODO for the future (maybe):
 *  - Recombs
 *  - Handle accessories like campfire badge having to be upgraded multiple times to upgrade rarity
 *  - Take into account requirements (hotm level, slayer level, etc)
 */

object AccessoriesHelperScreen : SkyOceanScreen() {

    val state: ListenableState<String> = ListenableState.of("")
    private lateinit var textBox: TextBox
    val dropdownState: DropdownState<TrackedAccessoryType> = DropdownState.of(ALL)
    val widgetWidth get() = (width / 3).coerceAtLeast(100) + 50
    val widgetHeight get() = (height / 3).coerceAtLeast(100) + 50
    var requireRebuild = true
    private var search: String? = null

    private var cycling = true

    val currentWidgets = mutableListOf<AbstractWidget>()

    val trackedAccessories = mutableListOf<TrackedAccessory>()

    override fun onClose() {
        super.onClose()
        requireRebuild = true
        search = null
        trackedAccessories.clear()
    }

    override fun init() {
        if (requireRebuild) rebuildItems()
        super.init()

        val width = widgetWidth
        val height = widgetHeight

        Displays.layered(
            background(olympus("buttons/normal"), width, height),
            background(olympus("buttons/dark/normal"), width, 26),
        ).asWidget().center().applyAsRenderable()

        val body = LayoutFactory.frame(width, height) {
            vertical {
                LayoutFactory.frame {
                    vertical(alignment = RIGHT) {
                        spacer(width)
                        LayoutFactory.horizontal {
                            spacer(height = 24)
                            textBox = Widgets.textInput(state) { box ->
                                box.withChangeCallback(::refreshSearch)
                                box.withPlaceholder("Search...")
                                box.withSize(100, 20)
                            }
                            textBox.add {
                                alignVerticallyMiddle()
                            }
                            spacer(width = 2)
                        }.add()
                    }
                    vertical(alignment = LEFT) {
                        spacer(width)
                        LayoutFactory.horizontal {
                            spacer(height = 24, width = 2)
                            Widgets.dropdown(
                                dropdownState,
                                TrackedAccessoryType.entries,
                                { modes ->
                                    Text.of(modes.name.toTitleCase())
                                },
                                { button -> button.withSize(80, 20) },
                            ) { builder ->
                                builder.withCallback { refresh() }
                            }.add {
                                alignVerticallyMiddle()
                            }
                            spacer(width = 2)
                        }.add()
                    }

                    vertical(alignment = MIDDLE) {
                        Widgets.text(Text.of("Accessories Helper", TextColor.WHITE)).add {
                            alignVerticallyMiddle()
                        }
                    }

                }.add()
                spacer(height = 2)
                spacer(width = 4, height - 26)
            }
            vertical {
                vertical(alignment = LEFT) {
                    spacer(width)
                    LayoutFactory.horizontal {
                        spacer(height = 24)
                    }.add()
                }
                spacer(height = 2)
                spacer(width = 4, height - 26)
            }
        }.center()
        body.applyLayout()

        addItems()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
        if (!McScreen.isOf<AccessoriesHelperScreen>()) {
            Displays.disableTooltips {
                super.render(graphics, mouseX, mouseY, f)
            }
        } else {
            super.render(graphics, mouseX, mouseY, f)
        }
    }


    fun addItems() {
        val width = widgetWidth
        val height = widgetHeight

        currentWidgets.forEach { this.removeWidget(it) }
        currentWidgets.clear()
        LayoutFactory.frame(width, height) {
            vertical {
                spacer(width, 26)
                horizontal {
                    spacer(width = 4, height - 26)
                    vertical(alignment = MIDDLE) {
                        buildItems(width - 10, height - 26)
                            .asScrollable(width - 5, height = height - 29, alwaysShowScrollBar = true)
                            .add()
                    }
                }
            }
        }.center().applyAndGetElements().let(currentWidgets::addAll)
    }

    fun buildItems(width: Int, height: Int): Layout {
        val columns = (width - 5) / 20
        val rows = (height - 5) / 20
        val filter = dropdownState.get() ?: TrackedAccessoryType.ALL

        // TODO: move marked to top
        val items = this.trackedAccessories.filter { it.type.matches(filter) }.mapNotNull { accessory ->
            if (!accessory.type.matches(filter)) return@mapNotNull null

            val tierItems = accessory.items.filter(::matchesSearch)

            fun createItemDisplay(item: ItemStack): Display {
                return Displays.item(
                    item,
                    customStackText = if (accessory.type == MISSING) AccessoriesHelper.AccessoryResult.MISSING.component!!
                    else AccessoriesHelper.AccessoryResult.UPGRADE.component!!
                ).withTooltip {
                    add(item.hoverName)
                    item.getLore().forEach(::add)
                    space()

                    when (accessory) {
                        is MissingAccessory -> {
                            add("Missing Accessory!", TextColor.RED)
                        }

                        is HasCurrentTier -> {
                            add("You can upgrade to this accessory!", TextColor.BLUE)
                            add("Current accessory: ") {
                                color = TextColor.BLUE
                                append(accessory.currentItem.hoverName)
                            }
                        }
                    }

                    space()
                    if (accessory.marked) {
                        add("Left click to unmark accessory!", TextColor.YELLOW)
                    } else {
                        add("Left click to set as marked", TextColor.YELLOW)
                    }


                }.withPadding(2)
            }

            val size = tierItems.size
            val itemDisplay = when  {
                size == 0 -> return@mapNotNull null
                size > 1 && cycling -> {
                    val itemDisplays = tierItems.map(::createItemDisplay)
                    Displays.supplied {
                        val seconds = System.currentTimeMillis() / 1000
                        itemDisplays[(seconds % itemDisplays.size).toInt()]
                    }
                }
                else -> createItemDisplay(tierItems.first())
            }

            val leftAction = { _: Button ->
                accessory.marked = !accessory.marked
                refresh()
            }

            val rightAction: (Button) -> Unit

            if (size == 1) {
                rightAction = { _: Button ->
                    // TODO: set single as crafthelper
                }
            } else {
                rightAction = { _: Button ->
                    ContextMenu.open { menu ->
                        menu.withAutoCloseOff()
                        val title = "Select CraftHelper Item"
                        menu.add { Widgets.text(title).withPadding(3) }
                        val dropdownState = DropdownState.empty<ItemStack>()
                        menu.add {
                            Widgets.dropdown(dropdownState, tierItems, { it.hoverName }, { it: Button ->
                                // TODO: make button size dependant on max width of item name
                                it.withSize(80, 20)
                            },
                                {
                                    it.withCallback { item ->
                                        Text.of("Selected ") {
                                            append(item.hoverName)
                                        }.send()
                                        // TODO: select crafthelper item
                                    }
                                })
                        }

                    }
                }
            }

            itemDisplay.asButton(leftAction, rightAction)
        }.rightPad(columns * rows, Displays.empty(20, 20).asWidget()).chunked(columns)

        return LayoutFactory.frame {
            display(
                ExtraDisplays.inventoryBackground(
                    columns,
                    items.size,
                    Displays.empty(columns * 20, items.size * 20).withPadding(2),
                ).withPadding(top = 5, bottom = 5),
            )
            items.asWidgetTable().add { alignVerticallyMiddle() }
        }
    }

    fun matchesSearch(item: ItemStack): Boolean {
        val search = search ?: return true
        if (item.cleanName.contains(search, true)) return true
        return item.getRawLore().any { it.contains(search, true) }
    }

    fun refreshSearch(search: String) {
        this.search = search.takeUnless { it.isEmpty() }
        addItems()
    }

    fun refresh() {
        addItems()
    }

    fun rebuildItems() {
        trackedAccessories.clear()
        trackedAccessories.addAll(AccessoriesHelper.getMissingAccessories().map(::MissingAccessory))
        trackedAccessories.addAll(AccessoriesHelper.getUpgradeableAccessories().map(::UpgradeAccessory))
        trackedAccessories.addAll(AccessoriesHelper.getUpgradeableRarityAccessories().map(::UpgradeRarityAccessory))

        requireRebuild = false
    }

}
