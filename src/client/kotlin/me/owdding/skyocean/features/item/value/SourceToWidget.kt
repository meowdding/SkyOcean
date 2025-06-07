package me.owdding.skyocean.features.item.value

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.string.TextWidget
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.extensions.shorten
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
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object SourceToWidget {

    fun text(string: String, init: MutableComponent.() -> Unit = {}) = text(Text.of(string, init))
    fun text(init: MutableComponent.() -> Unit) = text(Text.of(init))
    fun text(text: Component): TextWidget = Widgets.text(text)
    fun LayoutBuilder.text(string: String, init: MutableComponent.() -> Unit = {}) = text(Text.of(string, init))
    fun LayoutBuilder.text(init: MutableComponent.() -> Unit) = text(Text.of(init))
    fun LayoutBuilder.text(text: Component) = SourceToWidget.text(text).add()

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
                            this.color = TextColor.GOLD
                        }
                    }
                }
                text {
                    color = TextColor.DARK_GRAY
                    append(itemStack.hoverName.string)
                    append(": ")
                    append(price.shorten()) {
                        this.color = TextColor.GOLD
                    }
                }
            }

            is ItemWithLimitEntry -> {
                text {
                    color = TextColor.DARK_GRAY
                    append(itemStack.hoverName.string)
                    append(": ")
                    append(price.shorten()) {
                        this.color = TextColor.GOLD
                    }
                    append(" ($limit/$limit)") {
                        this.color = TextColor.GRAY
                    }
                }
            }

            is ReforgeEntry -> {
                text("Reforge: ${this.price.toFormattedString()}")
            }

            is CostEntries -> LayoutFactory.vertical {
                cost.forEach { cost ->
                    when (cost) {
                        is CoinCost -> text {
                            color = TextColor.DARK_GRAY
                            append("Coins") { this.color = TextColor.GOLD }
                            append(": ")
                            append(cost.amount.toFormattedString()) { this.color = TextColor.GOLD }
                        }

                        is EssenceCost -> text {
                            this.color = TextColor.DARK_GRAY
                            append("${cost.amount.toFormattedString()}x ")
                            append(RepoItemsAPI.getItem("ESSENCE_${cost.essenceType.name}").hoverName.string)
                            append(": ")
                            append(cost.amount.toFormattedString()) { this.color = TextColor.GOLD }
                        }

                        is ItemCost -> text {
                            color = TextColor.DARK_GRAY
                            append(RepoItemsAPI.getItem(cost.itemId).hoverName.string)
                            append(": ")
                            append(price.shorten()) {
                                this.color = TextColor.GOLD
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
                    this.color = TextColor.GOLD
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
                    ClickToExpandWidget(SourceToWidget.text("Conversion Cost:"), stars, callback).add()
                }
            }

            is GroupedEntry -> this.asWidget(callback)

            else -> {
                Widgets.text("Unknown Source: ${this.price.shorten()}")
            }
        }
    }

    val names = mapOf(
        ENCHANTMENT to ("Enchantment" to "s"),
    )

    private fun getName(source: ItemValueSource, amount: Int): String {
        if (names.containsKey(source)) {
            val pair = names[source]!!
            return if (amount != 1) {
                "${pair.first}${pair.second}"
            } else pair.first
        }

        return if (amount != 1) {
            "${source.name.toTitleCase()}s"
        } else source.name.toTitleCase()
    }

    private fun GroupedEntry.asWidget(callback: () -> Unit): LayoutElement {
        val filtered = entries.filter { it.price > 0 }
            .sortedByDescending { it.price }
        return when (this.source) {
            ENCHANTMENT, GEMSTONE, NECRON_SCROLLS, ITEM_STARS, DRILL_COMPONENTS, FISHING_ROD_PARTS -> {
                ClickToExpandWidget(
                    text("${filtered.size} ${getName(source, filtered.size)}: ") {
                        this.color = TextColor.DARK_GRAY
                        append(" ${price.shorten()}") { this.color = TextColor.GOLD }
                    },
                    LayoutFactory.vertical {
                        filtered.forEach {
                            widget(it.asWidget(callback))
                        }
                    },
                    callback,
                )
            }

            HOT_POTATO, REFORGE, RECOMBOBULATOR, SILEX, DIVAN_POWDER_COATING, POLARVOID, POWER_ABILITY_SCROLL, APPLIED_RUNE, APPLIED_DYE, HELMET_SKIN -> {
                LayoutFactory.vertical {
                    filtered.forEach {
                        widget(it.asWidget(callback))
                    }
                }
            }


            else -> {
                text("${this.source.name}: ${price.shorten()}")
            }
        }
    }

}
