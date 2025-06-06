package me.owdding.skyocean.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.notkamui.keval.*
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen
import me.owdding.skyocean.utils.ChatUtils
import me.owdding.skyocean.utils.Utils.getArgument
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object CalcCommand {

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("calc") {
            then("expression", StringArgumentType.greedyString()) {
                callback {
                    val expression: String = this.getArgument<String>("expression")!!
                    try {
                        val value = expression.keval {
                            includeDefault() // Todo maybe make some player stats available as variable
                        }
                        ChatUtils.chat("$expression = $value")
                    } catch (e: KevalException) {
                        val message = when (e) {
                            is KevalInvalidArgumentException -> "Invalid argument: ${e.message}"
                            is KevalZeroDivisionException -> "Division by zero :("
                            is KevalInvalidSymbolException -> "Invalid symbol: ${e.message}"
                            is KevalInvalidExpressionException -> "Invalid expression: ${e.message}"
                            is KevalDSLException -> "DSL error: ${e.message}"
                            else -> "Unknown error: ${e.message}"
                        }
                        ChatUtils.chat(message) { this.color = TextColor.RED }
                    }
                }
            }
        }
        event.registerWithCallback("balls") {
            McClient.setScreen(ItemSearchScreen)
        }
    }

}
