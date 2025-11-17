package me.owdding.skyocean.features.item.value

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.string.TextWidget
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.extensions.shorten
import me.owdding.lib.layouts.ClickToExpandWidget
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.chat.OceanColors.BETTER_GOLD
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.item.calculator.CalculationEntry
import tech.thatgravyboat.skyblockapi.api.item.calculator.CostEntries
import tech.thatgravyboat.skyblockapi.api.item.calculator.GemstoneSlotEntry
import tech.thatgravyboat.skyblockapi.api.item.calculator.GroupedEntry
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemEntry
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemStarEntry
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemValueSource.DRILL_COMPONENTS
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemValueSource.ENCHANTMENT
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemValueSource.FISHING_ROD_PARTS
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemValueSource.GEMSTONE
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemValueSource.ITEM_STARS
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemValueSource.NECRON_SCROLLS
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemWithLimitEntry
import tech.thatgravyboat.skyblockapi.api.item.calculator.ReforgeEntry
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.CoinCost
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.EssenceCost
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.ItemCost
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.BazaarAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object SourceToWidget {

    private val regex = Regex("(.*)_([^_]+)")
    private fun text(string: String, init: MutableComponent.() -> Unit = {}) = text(Text.of(string, init))
    private fun text(init: MutableComponent.() -> Unit) = text(Text.of(init))
    private fun text(text: Component): TextWidget = Widgets.text(text)
    private fun LayoutBuilder.text(string: String, init: MutableComponent.() -> Unit = {}) = text(Text.of(string, init))
    private fun LayoutBuilder.text(init: MutableComponent.() -> Unit) = text(Text.of(init))
    private fun LayoutBuilder.text(text: Component) = SourceToWidget.text(text).add()

    fun CalculationEntry.asWidget(callback: () -> Unit): LayoutElement {
        return when (this) {
            is ItemEntry -> {
                var id = this.itemId
                if (this.itemId.startsWith("ENCHANTMENT_")) {
                    id = id.substringAfter("_").replace(regex, "$1:$2")
                } else if (this.itemId.startsWith("rune:")) {
                    id = id.substringAfter(":")
                }
                val name = SkyBlockId.unknownType(id.lowercase())?.toItem()?.hoverName ?: !id
                text {
                    color = TextColor.DARK_GRAY
                    append(name.string)
                    append(": ")
                    append(price.shorten()) {
                        this.color = BETTER_GOLD
                    }
                }
            }

            is ItemWithLimitEntry -> {
                text {
                    color = TextColor.DARK_GRAY
                    append(itemStack.hoverName.string)
                    append(": ")
                    append(price.shorten()) {
                        this.color = BETTER_GOLD
                    }
                    append(" ($amount/$limit)") {
                        this.color = TextColor.DARK_GRAY
                    }
                }
            }

            is ReforgeEntry -> {
                LayoutFactory.vertical {
                    text {
                        color = TextColor.DARK_GRAY
                        append(RepoItemsAPI.getItemName(this@asWidget.reforge))
                        append(": ")
                        append(this@asWidget.price.toFormattedString()) {
                            this.color = BETTER_GOLD
                        }
                    }
                    val applyCost = this@asWidget.applyCost
                    if (applyCost > 0) {
                        text {
                            color = TextColor.DARK_GRAY
                            append(" Apply Cost: ")
                            append(applyCost.shorten()) {
                                this.color = BETTER_GOLD
                            }
                        }
                    }
                    text(" ")
                }
            }

            is CostEntries -> LayoutFactory.vertical {
                cost.forEach { cost ->
                    when (cost) {
                        is CoinCost -> text {
                            color = TextColor.DARK_GRAY
                            append("Coins") { this.color = BETTER_GOLD }
                            append(": ")
                            append(cost.amount.shorten()) { this.color = BETTER_GOLD }
                        }

                        is EssenceCost -> text {
                            this.color = TextColor.DARK_GRAY
                            append("${cost.amount.toFormattedString()}x ")
                            append(RepoItemsAPI.getItem(cost.essenceType.bazaarId ?: "").hoverName)
                            append(": ")
                            append(BazaarAPI.getProduct(cost.essenceType.bazaarId)?.sellPrice?.times(cost.amount)?.shorten() ?: "0") {
                                this.color = BETTER_GOLD
                            }
                        }

                        is ItemCost -> text {
                            color = TextColor.DARK_GRAY
                            append(RepoItemsAPI.getItem(cost.itemId).hoverName)
                            append(": ")
                            append(price.shorten()) {
                                this.color = BETTER_GOLD
                            }
                        }
                    }
                }
            }

            is GemstoneSlotEntry -> text {
                color = TextColor.DARK_GRAY
                append(itemStack.hoverName.string)
                append(": ")
                append(price.shorten()) {
                    this.color = BETTER_GOLD
                }
            }

            is ItemStarEntry -> LayoutFactory.vertical {
                fun addSection(header: String, body: Layout) {
                    text(header)
                    horizontal {
                        spacer(5)
                        body.add()
                    }
                }

                if (conversionCost != null) {
                    val conversion = LayoutFactory.horizontal {
                        spacer(5)
                        conversionCost!!.asWidget(callback).add()
                    }
                    if (stars.isEmpty()) {
                        conversion.add()
                    } else {
                        addSection("Conversion Cost:", conversion)
                    }
                }

                if (stars.isEmpty()) return@vertical

                val stars = LayoutFactory.vertical {
                    stars.sortedByDescending { it.price }.forEach { it.asWidget(callback).add() }
                }
                if (conversionCost == null) {
                    stars.add()
                } else {
                    addSection("Stars:", stars)
                }
            }

            is GroupedEntry -> this.asWidget(callback)

            else -> {
                Widgets.text("Unknown Source: ${this.price.shorten()}")
            }
        }
    }

    private fun GroupedEntry.asWidget(callback: () -> Unit): LayoutElement {
        val filtered = entries.filter { it.price > 0 }
            .sortedByDescending { it.price }
        return when (this.source) {
            ENCHANTMENT, GEMSTONE, NECRON_SCROLLS, ITEM_STARS, DRILL_COMPONENTS, FISHING_ROD_PARTS -> {
                ClickToExpandWidget(
                    text("${filtered.size} ${source.name.toTitleCase()}: ") {
                        this.color = TextColor.DARK_GRAY
                        append(price.shorten()) { this.color = BETTER_GOLD }
                    },
                    LayoutFactory.vertical {
                        filtered.forEach {
                            widget(it.asWidget(callback))
                        }
                    },
                    callback,
                )
            }

            else -> {
                LayoutFactory.vertical {
                    filtered.forEach {
                        widget(it.asWidget(callback))
                    }
                }
            }
        }
    }

}
