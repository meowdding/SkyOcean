package me.owdding.skyocean.features.environmental

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.environmental.EnvironmentalConfig
import me.owdding.skyocean.data.profile.WeatherAlertStorage
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.extensions.joinToComponent
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.environmental.WeatherAPI
import tech.thatgravyboat.skyblockapi.api.environmental.WeatherGroup
import tech.thatgravyboat.skyblockapi.api.environmental.WeatherIntensity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.command
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover

@Module
object WeatherAlertSystem {

    // TODO: smth in sbapi?
    private val warps = mapOf(
        WeatherGroup.DWARVEN_MINES to "mines",
        WeatherGroup.CRYSTAL_HOLLOWS to "nucleus",
        WeatherGroup.GLACITE_TUNNELS to "tunnels",
        WeatherGroup.SPIDERS_DEN to "spider",
        WeatherGroup.THE_END to "end",
        WeatherGroup.CRIMSON_ISLE to "isle",
        WeatherGroup.MOONGLADE_MARSH to "moonglade",
        WeatherGroup.BACKWATER_BAYOU to "bayou",
        WeatherGroup.LOTUS_ATOLL to "lotus",
        WeatherGroup.GARDEN to "garden",
    )

    private val alertedGroups = mutableSetOf<WeatherGroup>()
    private var wasActive = false

    @Subscription(TickEvent::class)
    @TimePassed("1s")
    @OnlyOnSkyBlock
    private fun onTick() {
        val isActive = WeatherAPI.isActive

        if (!isActive) {
            if (wasActive) {
                alertedGroups.clear()
                wasActive = false
            }
            return
        }

        wasActive = true

        val intensity = WeatherAPI.currentIntensity ?: return
        val currentGroup = WeatherGroup.getCurrentGroup()

        val triggeredAlerts = WeatherGroup.entries.mapNotNull { group ->
            if (group in alertedGroups) return@mapNotNull null

            if (group != currentGroup && !EnvironmentalConfig.alertOtherIslands) return@mapNotNull null

            if (WeatherAlertStorage.hasAlert(group, intensity)) {
                alertedGroups.add(group)
                return@mapNotNull group
            }
            return@mapNotNull null
        }.ifEmpty { return }

        if (triggeredAlerts.size == 1) {
            val group = triggeredAlerts.first()
            val event = if (intensity == WeatherIntensity.EXTREME) group.extreme else group.mild
            Text.of {
                append("A ")
                append(intensity.displayName, intensity.color)
                append(" ")
                append(event.type.weatherName, intensity.color)
                append(" weather event has started in ")
                append(group.formattedName, TextColor.GOLD)
                append("!")

                hover = Text.of("Click to warp", TextColor.GRAY)
                command = group.warp()
            }.sendWithPrefix("skyocean-weather-alert")
        } else {
            Text.of {
                append("A ")
                append(intensity.displayName, intensity.color)
                append(" weather event has started in ")
                append(
                    triggeredAlerts.joinToComponent(", ") {
                        val event = if (intensity == WeatherIntensity.EXTREME) it.extreme else it.mild
                        Text.of(it.formattedName) {
                            color = TextColor.GOLD
                            hover = Text.multiline(event.type.component, "", Text.of("Click to warp", TextColor.GRAY))
                            command = it.warp()
                        }
                    },
                )
                append("!")
                append(" (hover)", TextColor.GRAY)
            }.sendWithPrefix("skyocean-weather-alert")
        }

        if (EnvironmentalConfig.weatherTitleAlert) {
            val title = Text.of {
                append("Weather Alert: ", OceanColors.WARNING)
                bold = true
            }
            val subTitle = Text.of(triggeredAlerts.joinToString(", ") { it.formattedName })

            McClient.setTitle(title, subTitle)
        }

        McClient.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f)
    }

    private fun WeatherGroup.warp(): String = when (this) {
        WeatherGroup.JERRYS_WORKSHOP -> "savethejerrys"
        else -> "warp ${warps[this]}"
    }
}
