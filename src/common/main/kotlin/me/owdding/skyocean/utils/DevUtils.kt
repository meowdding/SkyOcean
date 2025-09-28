package me.owdding.skyocean.utils

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.commands.VirtualResourceArgument
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KProperty

internal fun debugToggle(path: String, description: String = path): DebugToggle {
    return DebugToggle(SkyOcean.id(path), description)
}

data class DebugToggle(val location: ResourceLocation, val description: String) {
    init {
        DevUtils.register(this)
    }

    operator fun getValue(any: Nothing?, property: KProperty<*>): Boolean {
        return DevUtils.isOn(location)
    }

    operator fun getValue(any: Any?, property: KProperty<*>): Boolean {
        return DevUtils.isOn(location)
    }

}

@Module
internal object DevUtils {
    internal val states = mutableMapOf<ResourceLocation, Boolean>()
    internal val toggles = mutableListOf<DebugToggle>()

    fun register(debugToggle: DebugToggle) {
        states[debugToggle.location] = false
        toggles += debugToggle
    }

    fun toggle(location: ResourceLocation) {
        states[location] = states[location]?.not() == true
    }

    fun isOn(location: ResourceLocation) = states.getOrDefault(location, false)

    @Subscription
    fun onCommandRegister(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("toggle") {
            then("location", VirtualResourceArgument(states.keys, SkyOcean.MOD_ID), DevToolSuggestionProvider) {
                callback {
                    val argument = this.getArgument("location", ResourceLocation::class.java)
                    toggle(argument)
                    ChatUtils.chat(
                        Text.of("Toggled ") {
                            append(argument.toString()) {
                                this.color = TextColor.GOLD
                            }
                            if (isOn(argument)) {
                                append(" on") { this.color = TextColor.GREEN }
                            } else {
                                append(" off") { this.color = TextColor.RED }
                            }
                        },
                    )
                }
            }
        }
    }
}

internal object DevToolSuggestionProvider : SuggestionProvider<FabricClientCommandSource> {
    override fun getSuggestions(context: CommandContext<FabricClientCommandSource>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        fun matches(arg: String): Boolean = SharedSuggestionProvider.matchesSubStr(builder.remaining.lowercase(), arg)


        DevUtils.toggles.forEach {
            if (matches(it.location.toString().lowercase()) || matches(it.location.path.lowercase())) {
                builder.suggest(it.location.toString()) { it.description }
            }
        }

        return builder.buildFuture()
    }
}
