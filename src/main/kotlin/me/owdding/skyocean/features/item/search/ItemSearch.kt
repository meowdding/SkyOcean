package me.owdding.skyocean.features.item.search

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.ComponentFactory
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.search.highlight.ItemHighlighter
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen
import me.owdding.skyocean.features.item.search.search.tag.SearchTagsParser
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent.Companion.argument
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick

@Module
object ItemSearch {

    val key = SkyOceanKeybind("item_search", InputConstants.KEY_O) keybind@{
        if (!LocationAPI.isOnSkyBlock) return@keybind
        McClient.setScreen(ItemSearchScreen)
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("search") {
            callback {
                if (!LocationAPI.isOnSkyBlock) {
                    text("You must be on Skyblock!") { this.color = TextColor.RED }
                    return@callback
                }
                McClient.setScreen(ItemSearchScreen)
            }
            thenCallback("reset") {
                ItemHighlighter.resetSearch()
                text("Reset search!").sendWithPrefix()
            }
            thenCallback("string", StringArgumentType.greedyString()) {
                val string = argument<String>("string")
                val (filter, exceptions) = SearchTagsParser.parse(string) ?: return@thenCallback
                // we don't cancel the item search even if it has exceptions because we still have a partial item filter result
                if (exceptions.isNotEmpty()) {
                    text("Error while parsing search tags (${exceptions.size})") {
                        color = OceanColors.WARNING
                        hover = ComponentFactory.multiline {
                            exceptions.take(5).forEach {
                                string("- ${it.message}") {
                                    color = TextColor.RED
                                }
                            }
                            if (exceptions.size > 5) {
                                string("... (and ${exceptions.size} - 5 more)") {
                                    color = TextColor.DARK_GRAY
                                }
                            }
                            newLine()
                            string("Click to copy the errors to clipboard!") {
                                color = TextColor.YELLOW
                            }
                        }
                        onClick {
                            McClient.clipboard = exceptions.joinToString("\n") {
                                it.message ?: it.toString()
                            }
                            text("Copied search tag parsing errors to clipboard!").sendWithPrefix()
                        }
                    }
                }
                // We can apply the search before opening the screen, as it only resets the search on close and not open
                ItemSearchScreen.search = filter
                ItemSearchScreen.state.set(string)
                McClient.setScreen(ItemSearchScreen)
            }
        }
    }

}
