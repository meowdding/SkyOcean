package me.owdding.skyocean.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.notkamui.keval.KevalDSLException
import com.notkamui.keval.KevalException
import com.notkamui.keval.KevalInvalidArgumentException
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException
import com.notkamui.keval.keval
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.Utils.exclusiveInclusive
import me.owdding.skyocean.utils.Utils.getArgument
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object CalcCommand {

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("powdercalc") {
            then("maxLevel", IntegerArgumentType.integer(5)) {
                then("expression", StringArgumentType.greedyString()) {
                    callback {
                        (1 exclusiveInclusive getArgument<Int>("maxLevel")!!).sumOf {
                            getArgument<String>("expression")!!.keval {
                                includeDefault()
                                constant {
                                    name = "nextLevel"
                                    value = it.toDouble()
                                }
                            }.toInt()
                        }.toFormattedString().asComponent().sendWithPrefix()
                    }
                }
            }
        }
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
    }

}
