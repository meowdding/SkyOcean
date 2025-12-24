package me.owdding.skyocean.features.item.search.screen

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.components.textbox.TextBox
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.ui.context.ContextMenu
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
import me.owdding.lib.layouts.ScalableWidget
import me.owdding.lib.layouts.withPadding
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.features.item.search.highlight.ItemHighlighter
import me.owdding.skyocean.features.item.search.matcher.ItemMatcher
import me.owdding.skyocean.features.item.search.search.ReferenceItemFilter
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.item.sources.SackItemContext
import me.owdding.skyocean.features.item.sources.system.BundledItemContext
import me.owdding.skyocean.features.item.sources.system.TrackedItem
import me.owdding.skyocean.features.item.sources.system.TrackedItemBundle
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.asWidgetTable
import me.owdding.skyocean.utils.extensions.asScrollable
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object ItemSearchScreen : SkyOceanScreen() {
    val state: ListenableState<String> = ListenableState.of("")
    val dropdownState: DropdownState<SortModes> = DropdownState.of(SortModes.AMOUNT)
    val ascending: ListenableState<Boolean> = ListenableState.of(true)
    var search: String? = null
    var category: SearchCategory = SearchCategory.ALL
    val currentWidgets = mutableListOf<AbstractWidget>()

    val widgetWidth get() = (width / 3).coerceAtLeast(100) + 50
    val widgetHeight get() = (height / 3).coerceAtLeast(100) + 50
    var requireRebuild = true
    val items = mutableListOf<TrackedItem>()

    private lateinit var textBox: TextBox

    override fun onClose() {
        super.onClose()
        if (!MiscConfig.preserveLastSearch) {
            this.search = null
            this.state.set("")
        }
        this.category = SearchCategory.ALL
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
        if (SkyBlockIsland.THE_RIFT.inIsland()) this.category = SearchCategory.RIFT

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
                                    McClient.runNextTick {
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
                                    graphics.drawSprite(
                                        texture,
                                        widget.x + 5,
                                        widget.y + 5,
                                        10,
                                        10,
                                        ARGB.opaque(TextColor.DARK_GRAY),
                                    )
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
        }.center()
        body.applyLayout()

        LayoutFactory.vertical {
            SearchCategory.entries.forEach { category ->
                val button = Button().apply {
                    val display = Displays.item(category.icon, 16, 16).withPadding(2)

                    val selected = WidgetRenderers.sprite<Button>(UIConstants.PRIMARY_BUTTON)
                    val normal = WidgetRenderers.sprite<Button>(UIConstants.BUTTON)

                    // TODO: dont make this so awful
                    withRenderer(
                        WidgetRenderers.layered(
                            { graphics, widget, ticks ->
                                (if (this@ItemSearchScreen.category == category) selected else normal).render(graphics, widget, ticks)
                            },
                            DisplayWidget.displayRenderer(display),
                        ),
                    )
                    withTooltip(Text.of(category.toFormattedName()))
                    setSize(20, 20)
                    withTexture(null)
                    withCallback {
                        this@ItemSearchScreen.category = category
                        rebuildItems()
                        addItems()
                    }
                }
                widget(button)
            }
        }.apply {
            setPosition(body.x - 20, body.y + 26)
            applyLayout()
        }
        addItems()
    }

    override fun setInitialFocus() {
        val widget = this.children().find { ((it as? ScalableWidget)?.original ?: it) == textBox } ?: return
        focused = widget
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
                            .asScrollable(width - 5, height = height - 29, alwaysShowScrollBar = true)
                            .add()
                    }
                }
            }
        }.center().applyAndGetElements().let(currentWidgets::addAll)
    }

    fun matches(itemStack: ItemStack): Boolean {
        val search = search ?: return true
        if (itemStack.cleanName.contains(search, true)) return true
        return itemStack.getRawLore().any { it.contains(search, true) }
    }

    fun buildItems(width: Int, height: Int): Layout {
        val columns = (width - 5) / 20
        val rows = (height - 5) / 20

        val items = this.items.filter { (itemStack) -> matches(itemStack) }.map { (itemStack, context, price) ->
            val item = Displays.item(
                itemStack,
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
            }.withPadding(2)


            val leftAction = { _: Button ->
                ItemHighlighter.setHighlight(ReferenceItemFilter.create(context, itemStack))
                context.open()
                onClose()
            }

            if (context is SackItemContext || (context is BundledItemContext && context.map.containsKey(ItemSources.SACKS))) {
                item.asButton(leftAction) {
                    ContextMenu.open { menu ->
                        menu.withAutoCloseOff()
                        val title = "Get From Sacks"
                        menu.add { Widgets.text(title).withPadding(3) }
                        menu.add {
                            val state = ListenableState.of("")
                            Widgets.textInput(state) {
                                it.withSize(McFont.width(title), 20)
                                it.withEnterCallback {
                                    McClient.sendCommand("/gfs ${itemStack.getSkyBlockId()} ${state.get().parseFormattedLong()}")
                                    menu.onClose()
                                }
                            }.withPadding(3)
                        }
                    }
                }
            } else {
                item.asButtonLeft(leftAction)
            }
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

    fun refreshSearch(search: String) {
        this.search = search.takeUnless { it.isEmpty() }
        addItems()
    }

    fun refreshSort(mode: SortModes = dropdownState.get() ?: SortModes.AMOUNT) {
        this.items.sortWith(mode.let { if (ascending.get() == true) it else it.reversed() })
        addItems()
    }
}
