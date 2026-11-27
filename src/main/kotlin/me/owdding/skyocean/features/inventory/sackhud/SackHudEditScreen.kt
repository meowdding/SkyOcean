package me.owdding.skyocean.features.inventory.sackhud

import com.mojang.brigadier.arguments.StringArgumentType
import earth.terrarium.olympus.client.components.textbox.TextBox
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.Displays.background
import me.owdding.lib.displays.asButtonLeft
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.withPadding
import me.owdding.skyocean.config.features.inventory.SackHudConfig
import me.owdding.skyocean.config.features.inventory.SackHudConfig.EditorTheme
import me.owdding.skyocean.data.profile.SackHudStorage
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.repo.SackData
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.extensions.asScrollable
import me.owdding.skyocean.utils.suggestions.SackItemSuggestionProvider
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.text.Text

class SackHudEditScreen : SkyOceanScreen("Sack HUD Editor") {

    private val leftState: ListenableState<String> = ListenableState.of("")
    private val rightState: ListenableState<String> = ListenableState.of("")
    private var leftSearch = ""
    private var rightSearch = ""

    private val scrollGetters = arrayOf({ 0 }, { 0 })
    private val leftWidgets = mutableListOf<AbstractWidget>()
    private val rightWidgets = mutableListOf<AbstractWidget>()
    private val panelWidth get() = (width * 0.65).toInt().coerceAtLeast(320)
    private val panelHeight get() = (height * 0.75).toInt().coerceAtLeast(220)
    private val columnWidth get() = (panelWidth - 21) / 2

    private val listHeight get() = panelHeight - 62
    private val originX get() = (width - panelWidth) / 2
    private val originY get() = (height - panelHeight) / 2

    private val dark get() = SackHudConfig.editorTheme == EditorTheme.DARK
    private fun columnX(left: Boolean) = originX + 8 + if (left) 0 else columnWidth + 5

    override fun init() {
        super.init()
        background(olympus(if (dark) "buttons/dark/normal" else "buttons/normal"), panelWidth, panelHeight).asWidget().center().applyAsRenderable()
        buildColumn(left = true)
        buildColumn(left = false)
    }

    private fun buildColumn(left: Boolean) {
        val state = if (left) leftState else rightState

        val textBox = TextBox(state).apply {
            withPlaceholder("Search...")
            withSize(columnWidth - 10, 20)
            withChangeCallback {
                if (left) leftSearch = it else rightSearch = it
                scrollGetters[if (left) 0 else 1] = { 0 }
                rebuildList(left)
            }
        }

        LayoutFactory.vertical {
            display(Displays.text(if (left) +"overlays.sack_hud.selected" else +"overlays.sack_hud.available"))
            spacer(height = 4)
            widget(textBox)
        }.apply {
            setPosition(columnX(left), originY + 8)
            arrangeElements()
            visitWidgets(this@SackHudEditScreen::addRenderableWidget)
        }

        rebuildList(left)
    }

    private fun rebuildList(left: Boolean) {
        val scrollIndex = if (left) 0 else 1
        val previousScroll = scrollGetters[scrollIndex]()
        val tracked = if (left) leftWidgets else rightWidgets
        tracked.forEach(this::removeWidget)
        tracked.clear()

        val query = if (left) leftSearch else rightSearch
        val selected = SackHudStorage.items
        val source: Map<SkyBlockId, ItemStack> = if (left) {
            selected.associateWith { SackData.sackItems[it]?.create() ?: Items.BARRIER.defaultInstance }
        } else {
            SackData.sackItems.mapValues { it.value.create() }
        }

        val entries = source.filter { (id, stack) ->
            query.isEmpty() || id.cleanId.contains(query, true) || stack.cleanName.contains(query, true)
        }.entries.toList()

        val width = columnWidth - 10
        val rows = LayoutFactory.vertical {
            entries.forEach { (id, stack) ->
                val content = DisplayFactory.vertical {
                    spacer(width = width - 4)
                    horizontal(5, Alignment.CENTER) {
                        display(Displays.item(stack))
                        string(stack.hoverName)
                        val used = 16 + 5 + McFont.width(stack.hoverName)
                        if (width - 4 > used) spacer(width = width - 4 - used)
                    }
                }
                content.withPadding(2).asButtonLeft {
                    if (id in SackHudStorage.items) SackHudStorage.remove(id) else SackHudStorage.add(id)
                    rebuildList(left = true)
                    rebuildList(left = false)
                }.let { widget(it) }
            }
        }

        val list = rows.asScrollable(
            columnWidth,
            listHeight,
            init = {
                withScrollY(previousScroll)
                scrollGetters[scrollIndex] = { yScroll }
            },
            alwaysShowScrollBar = true,
        ).apply {
            setPosition(columnX(left), originY + 56)
            arrangeElements()
        }
        list.visitWidgets {
            addRenderableWidget(it)
            tracked.add(it)
        }
    }

    @Module
    companion object {
        @Subscription
        private fun onCommand(event: RegisterSkyOceanCommandEvent) {
            event.command("sackhud") {
                execute {
                    McClient.setScreenAsync { SackHudEditScreen() }
                }

                "add item"(StringArgumentType.greedyString(), SackItemSuggestionProvider) executes { input ->
                    val (id, name) = findSackItem(input) ?: return@executes notFound(input)
                    if (id in SackHudStorage.items) {
                        Text.of("$name is already in the sack HUD!", CatppuccinColors.Mocha.red).sendWithPrefix()
                        return@executes
                    }
                    SackHudStorage.add(id)
                    Text.of("Added $name to the sack HUD!", CatppuccinColors.Mocha.green).sendWithPrefix()
                }

                "remove item"(StringArgumentType.greedyString(), SackItemSuggestionProvider) executes { input ->
                    val (id, name) = findSackItem(input) ?: return@executes notFound(input)
                    if (id !in SackHudStorage.items) {
                        Text.of("$name is not in the sack HUD!", CatppuccinColors.Mocha.red).sendWithPrefix()
                        return@executes
                    }
                    SackHudStorage.remove(id)
                    Text.of("Removed $name from the sack HUD!", CatppuccinColors.Mocha.green).sendWithPrefix()
                }

                "clear" executes {
                    SackHudStorage.clear()
                    Text.of("Cleared the sack HUD!", CatppuccinColors.Mocha.green).sendWithPrefix()
                }
            }
        }

        private fun findSackItem(input: String): Pair<SkyBlockId, String>? {
            val query = input.trim()
            return SackData.sackItems.firstNotNullOfOrNull { (id, item) ->
                val name = item.create().cleanName
                if (id.cleanId.equals(query, ignoreCase = true) || name.equals(query, ignoreCase = true)) id to name else null
            }
        }
        private fun notFound(input: String) = Text.of("Couldn't find a sack item called $input", CatppuccinColors.Mocha.red).sendWithPrefix()
    }
}
