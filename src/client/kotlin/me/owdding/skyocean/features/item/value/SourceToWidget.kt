package me.owdding.skyocean.features.item.value

import earth.terrarium.olympus.client.components.Widgets
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.extensions.shorten
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.api.item.calculator.*
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object SourceToWidget {

    fun CalculationEntry.asWidget(callback: () -> Unit): LayoutElement {
        return when (this) {
            is ItemEntry -> {
                Widgets.text(Text.of {
                    color = TextColor.DARK_GRAY
                    append(itemStack.hoverName)
                    append(": ")
                    append(price.shorten()) {
                        this.color = TextColor.GOLD
                    }
                })
            }

            is ItemWithLimitEntry -> {
                Widgets.text(Text.of {
                    color = TextColor.DARK_GRAY
                    append(itemStack.hoverName)
                    append(": ")
                    append(price.shorten()) {
                        this.color = TextColor.GOLD
                    }
                    append(" ($limit/$limit)") {
                        this.color = TextColor.GRAY
                    }
                })
            }

            is ReforgeEntry -> {
                Widgets.text("Reforge: ${this.price.toFormattedString()}")
            }

            is CostEntries -> {
                Widgets.text("a")
            }

            is GemstoneSlotEntry -> {
                Widgets.text(Text.of {
                    color = TextColor.DARK_GRAY
                    append(itemStack.hoverName)
                    append(": ")
                    append(price.shorten()) {
                        this.color = TextColor.GOLD
                    }
                })
            }

            is ItemStarEntry -> {
                Widgets.text("c")
            }

            is GroupedEntry -> this.asWidget(callback)

            else -> {
                Widgets.text("Unknown Source: ${this.price.shorten()}")
            }
        }
    }

    private fun GroupedEntry.asWidget(callback: () -> Unit): LayoutElement {
        return when (this.source) {
            ItemValueSource.ENCHANTMENT -> {
                val enchants = this.entries.filterIsInstance<ItemEntry>()
                CLickToExpandWidget(
                    Widgets.text(
                        Text.of("${enchants.size} Enchantments: ") {
                            this.color = TextColor.DARK_GRAY
                            append(" ${price.shorten()}") { this.color = TextColor.GOLD }
                        },
                    ),
                    LayoutFactory.vertical {
                        enchants.filter { it.price > 0 }
                            .sortedByDescending { it.price }
                            .forEach {
                                widget(it.asWidget(callback))
                            }
                    },
                    callback,
                )
            }

            ItemValueSource.GEMSTONE -> {
                val enchants = this.entries.filterIsInstance<GemstoneSlotEntry>()
                CLickToExpandWidget(
                    Widgets.text(
                        Text.of("${enchants.size} Gemstones: ") {
                            this.color = TextColor.DARK_GRAY
                            append(" ${price.shorten()}") { this.color = TextColor.GOLD }
                        },
                    ),
                    LayoutFactory.vertical {
                        enchants.filter { it.price > 0 }
                            .sortedByDescending { it.price }
                            .forEach {
                                widget(it.asWidget(callback))
                            }
                    },
                    callback,
                )
            }

            else -> {
                Widgets.text("${this.source.name}: ${price.shorten()}")
            }
        }
    }

}
