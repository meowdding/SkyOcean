package me.owdding.skyocean.features.item.value

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.string.TextWidget
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.extensions.shorten
import me.owdding.lib.layouts.ClickToExpandWidget
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.item.calculator.*
import tech.thatgravyboat.skyblockapi.api.item.calculator.ItemValueSource.*
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoRunesAPI
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

    const val BETTER_GOLD = 0xfc6f03
    
    private fun text(string: String, init: MutableComponent.() -> Unit = {}) = text(Text.of(string, init))
    private fun text(init: MutableComponent.() -> Unit) = text(Text.of(init))
    private fun text(text: Component): TextWidget = Widgets.text(text)
    private fun LayoutBuilder.text(string: String, init: MutableComponent.() -> Unit = {}) = text(Text.of(string, init))
    private fun LayoutBuilder.text(init: MutableComponent.() -> Unit) = text(Text.of(init))
    private fun LayoutBuilder.text(text: Component) = SourceToWidget.text(text).add()

    fun CalculationEntry.asWidget(callback: () -> Unit): LayoutElement {
        return when (this) {
            is ItemEntry -> {
                if (this.itemId.startsWith("rune:")) {
                    val rune = RepoRunesAPI.getRune(string = this.itemId)
                    return text {
                        color = TextColor.DARK_GRAY
                        append(rune?.name ?: itemId.removePrefix("rune:"))
                        append(": ")
                        append(price.shorten()) {
                            this.color = BETTER_GOLD
                        }
                    }
                }
                text {
                    color = TextColor.DARK_GRAY
                    append(itemStack.hoverName.string)
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
                if (conversionCost != null) {
                    val conversion = LayoutFactory.horizontal {
                        spacer(5)
                        conversionCost!!.asWidget(callback).add()
                    }
                    if (stars.isEmpty()) {
                        conversion.add()
                    } else {
                        ClickToExpandWidget(SourceToWidget.text("Conversion Cost:"), conversion, callback).add()
                    }
                }

                val stars = LayoutFactory.vertical {
                    stars.sortedByDescending { it.price }.forEach { it.asWidget(callback).add() }
                }
                if (conversionCost == null) {
                    stars.add()
                } else {
                    ClickToExpandWidget(SourceToWidget.text("Stars:"), stars, callback).add()
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
