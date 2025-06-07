package me.owdding.skyocean.features.item.search.screen

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.StateUtils
import me.owdding.lib.builder.LEFT
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.builder.RIGHT
import me.owdding.lib.displays.*
import me.owdding.lib.displays.Displays.background
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.shorten
import me.owdding.skyocean.SkyOcean.olympus
import me.owdding.skyocean.features.item.search.highlight.ItemHighlighter
import me.owdding.skyocean.features.item.search.item.TrackedItem
import me.owdding.skyocean.features.item.search.item.TrackedItemBundle
import me.owdding.skyocean.features.item.search.matcher.ItemMatcher
import me.owdding.skyocean.features.item.search.search.ReferenceItemFilter
import me.owdding.skyocean.features.item.search.soures.ItemSources
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.asTable
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object ItemSearchScreen : SkyOceanScreen() {
    val state: ListenableState<String> = ListenableState.of("")
    val dropdownState: DropdownState<SortModes> = DropdownState.of(SortModes.AMOUNT)
    val ascending: ListenableState<Boolean> = ListenableState.of(true)
    var search: String? = null
    val currentWidgets = mutableListOf<AbstractWidget>()

    val widgetWidth get() = (width / 3).coerceAtLeast(100) + 50
    val widgetHeight get() = (height / 3).coerceAtLeast(100) + 50
    var requireRebuild = true
    val items = mutableListOf<TrackedItem>()

    override fun onClose() {
        super.onClose()
        this.search = null
        this.requireRebuild = true
        items.clear()
    }

    fun rebuildItems() {
        items.clear()
        items.addAll(
            ItemSources.getAllItems().fold(mutableListOf()) { list, item ->
                val (itemStack) = item

                list.find { ItemMatcher.compare(it.itemStack, itemStack) }?.let {
                    if (it !is TrackedItemBundle) {
                        list.remove(it)
                        list.add(it.add(item))
                    } else {
                        it.add(item)
                    }
                    return@fold list
                }

                list.add(item)
                list
            },
        )
        this.refreshSort()
        this.requireRebuild = false
    }

    override fun init() {
        if (requireRebuild) {
            rebuildItems()
        }

        super.init()
        val width = widgetWidth
        val height = widgetHeight

        Displays.layered(
            background(olympus("buttons/normal"), width, height),
            background(olympus("buttons/dark/normal"), width, 26),
        ).asWidget().center().applyAsRenderable()

        LayoutFactory.frame(width, height) {
            vertical {
                LayoutFactory.frame {
                    vertical(alignment = RIGHT) {
                        spacer(width)
                        LayoutFactory.horizontal {
                            spacer(height = 24)
                            Widgets.textInput(state) { box ->
                                box.withChangeCallback(::refreshSearch)
                                box.withPlaceholder("Search...")
                                box.withSize(100, 20)
                            }.add {
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
                                SortModes.entries,
                                { modes ->
                                    Text.of(modes.name.toTitleCase())
                                },
                                { button -> button.withSize(80, 20) },
                            ) { builder ->
                                builder.withCallback(::refreshSort)
                            }.add {
                                alignVerticallyMiddle()
                            }
                            Widgets.button { factory ->
                                factory.withCallback {
                                    StateUtils.booleanToggle(ascending).run()
                                    refreshSort()
                                    McClient.tell {
                                        // this is done because minecraft keeps things focused when they are clicked once,
                                        // since that in our case changes the texture we manually remove the focus again.
                                        factory.isFocused = false
                                    }
                                }
                                factory.withRenderer { graphics, widget, partialTick ->
                                    val texture = if (ascending.get()) {
                                        UIIcons.CHEVRON_UP
                                    } else {
                                        UIIcons.CHEVRON_DOWN
                                    }
                                    graphics.blitSprite(RenderType::guiTextured, texture, widget.x + 5, widget.y + 5, 10, 10, ARGB.opaque(TextColor.DARK_GRAY))
                                }
                                factory.withSize(20)
                            }.add {
                                alignVerticallyMiddle()
                            }
                            spacer(width = 2)
                        }.add()
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
        }.center().applyLayout()
        addItems()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
        if (McClient.self.screen !is ItemSearchScreen) {
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
                            .asScrollable(width - 5, height = height - 29, allwaysShowScrollBar = true)
                            .add()
                    }
                }
            }
        }.center().applyAndGetElements().let(currentWidgets::addAll)
    }

    fun matches(itemStack: ItemStack): Boolean {
        val search = search ?: return true
        return itemStack.cleanName.contains(search, true)
    }

    fun buildItems(width: Int, height: Int): Layout {
        val columns = (width - 5) / 20
        val rows = (height - 5) / 20
        val items = this.items.filter { (itemStack) -> matches(itemStack) }.map { (itemStack, context, price) ->
            Displays.item(
                itemStack,
                showStackSize = false,
                customStackText = if (itemStack.count > 1) itemStack.count.shorten(0) else null,
            ).withTooltip {
                add(itemStack.hoverName)
                itemStack.getLore().forEach(::add)
                space()
                val count = itemStack.count
                when (dropdownState.get()) {
                    SortModes.PRICE -> {
                        if (count > 1) {
                            val avg = price / count.toFloat()
                            add("Avg. Price Per: ${avg.shorten()}") { this.color = TextColor.GRAY }
                        }
                        add("Total Price: ${price.shorten()}") { this.color = TextColor.GRAY }
                        space()
                    }

                    SortModes.AMOUNT -> {
                        add("Amount: ${count.toFormattedString()}") { this.color = TextColor.GRAY }
                        space()
                    }

                    else -> {}
                }
                context.collectLines().forEach(::add)
            }.withPadding(2).asButton { button ->
                ItemHighlighter.setHighlight(ReferenceItemFilter(itemStack))
                context.open()
                McClient.setScreen(null)
            }
        }.rightPad(columns * rows, Displays.empty(20, 20).asWidget()).chunked(columns)
        return LayoutFactory.frame {
            items.asTable().add { alignVerticallyMiddle() }
            display(
                ExtraDisplays.inventoryBackground(columns, items.size, Displays.empty(columns * 20, items.size * 20).withPadding(2))
                    .withPadding(top = 5, bottom = 5),
            )
        }
    }

    fun refreshSearch(search: String) {
        this.search = search.takeUnless { it.isEmpty() }
        addItems()
    }

    fun refreshSort(mode: SortModes = dropdownState.get() ?: SortModes.AMOUNT) {
        this.items.sortWith(mode.let { if (ascending.get() == true) it else it.reversed() })
        addItems()
    }
}
