package me.owdding.skyocean.features.environmental

import com.mojang.authlib.properties.Property
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LEFT
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.builder.RIGHT
import me.owdding.lib.builder.VerticalLayoutBuilder
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.Displays.background
import me.owdding.lib.displays.asButtonLeft
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.withTooltip
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.data.profile.WeatherAlertStorage
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.extensions.asScrollable
import net.minecraft.network.chat.Component
import net.minecraft.world.item.component.ResolvableProfile
import tech.thatgravyboat.skyblockapi.api.environmental.DateTimeAPI
import tech.thatgravyboat.skyblockapi.api.environmental.SkyBlockInstant
import tech.thatgravyboat.skyblockapi.api.environmental.WeatherAPI
import tech.thatgravyboat.skyblockapi.api.environmental.WeatherEvent
import tech.thatgravyboat.skyblockapi.api.environmental.WeatherGroup
import tech.thatgravyboat.skyblockapi.api.environmental.WeatherIntensity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.GameProfile
import tech.thatgravyboat.skyblockapi.platform.toResolvableProfile
import tech.thatgravyboat.skyblockapi.utils.command.EnumArgument
import tech.thatgravyboat.skyblockapi.utils.extentions.toReadableTime
import tech.thatgravyboat.skyblockapi.utils.extentions.until
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import java.util.*
import kotlin.random.Random

class WeatherScreen : SkyOceanScreen() {
    private val widgetWidth get() = (width * 0.85).toInt().coerceAtLeast(400)
    private val widgetHeight get() = (height * 0.85).toInt().coerceAtLeast(250)

    private val weekDays = listOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday",
    )

    private val foreCastType = when (Random.nextDouble()) {
        in 0.0..0.1 -> "Pawcast"
        else -> "Forecast"
    }

    override fun init() {
        super.init()
        val frameWidth = widgetWidth
        val frameHeight = widgetHeight

        Displays.layered(
            background(SkyOcean.id("hotkey/header"), frameWidth, frameHeight),
        ).asWidget().center().applyAsRenderable()

        val body = LayoutFactory.frame(frameWidth, frameHeight) {
            vertical {
                spacer(width = frameWidth, height = 4)
                addTitle(frameWidth)
                spacer(height = 10)
                addBody(frameWidth, frameHeight)
                spacer(height = 4)
                addFooter(frameWidth)
            }
        }.center()

        body.applyAndGetElements().forEach { addRenderableWidget(it) }
    }

    private fun VerticalLayoutBuilder.addTitle(frameWidth: Int) {
        LayoutFactory.frame {
            display(background(SkyOcean.id("hotkey/0/button"), frameWidth, 50))
            horizontal(alignment = MIDDLE, spacing = 10) {
                LayoutFactory.frame(70, 70) {
                    display(background(SkyOcean.id("hotkey/3/button"), 70, 70))
                    vertical {
                        listOf("The", "SkyOcean", "Weather", "Channel").forEach {
                            display(Displays.text(Text.of(it)))
                        }
                    }
                }.add()

                vertical(alignment = LEFT) {
                    display(Displays.text(Text.of("Hypixel SkyBlock", TextColor.WHITE)))
                    horizontal {
                        display(Displays.text(Text.of("Extended $foreCastType", TextColor.GOLD)))
                        spacer(width = 10)
                        widget(
                            Widgets.text(
                                Text.of("Brought to you by: ") {
                                    color = TextColor.WHITE
                                    append(Text.player(ResolvableProfile.createUnresolved(UUID.fromString("2c3c97d7-2b1c-4355-b856-dae991ddf5db")))) {
                                        hover = Text.of("ALAND_", TextColor.RED)
                                    }
                                    append(" & ")
                                    append(
                                        Text.player(
                                            GameProfile {
                                                put(
                                                    "textures",
                                                    Property(
                                                        "textures",
                                                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGViMmU0ZGIzYmNmYjA1MDNmMWZmZmIzMzk4ODhlNWQ0MzIzOThiMjc2OTljNWExYTFjZDgwNzljOGQ5N2QifX19",
                                                    ),
                                                )
                                            }.toResolvableProfile(),
                                        ),
                                    ) {
                                        hover = Text.of("Sunny", TextColor.GOLD)
                                    }
                                },
                            ) { it.withShadow() },
                        )
                    }
                }

                if (DateTimeAPI.season != null) { // If we know the Season, surely we know the rest
                    vertical(alignment = RIGHT) {
                        display(Displays.text(Text.of("${weekDays.getOrNull((DateTimeAPI.day - 1) % 7)}", TextColor.WHITE)))
                        display(Displays.text(Text.of("${DateTimeAPI.season} ${DateTimeAPI.day}, Year ${SkyBlockInstant.now().year}")))
                    }
                }
            }
        }.add()
    }

    private fun VerticalLayoutBuilder.addBody(frameWidth: Int, frameHeight: Int) {
        val listWidth = frameWidth - 20
        val cardWidth = 100
        val columns = (listWidth / (cardWidth + 4)).coerceAtLeast(1)
        val listHeight = frameHeight - 100

        LayoutFactory.vertical {
            WeatherGroup.entries.chunked(columns).forEach { row ->
                horizontal(alignment = MIDDLE) {
                    row.forEach { group ->
                        LayoutFactory.frame(cardWidth, 140) {
                            display(background(SkyOcean.id("hotkey/3/background"), cardWidth, 140))

                            vertical(alignment = MIDDLE) {
                                spacer(width = cardWidth, height = 6)

                                display(Displays.text(Text.of(group.formattedName, TextColor.YELLOW)))

                                spacer(height = 10)

                                val (title, event) = if (WeatherAPI.isActive) {
                                    val event = when (WeatherAPI.currentIntensity) {
                                        WeatherIntensity.EXTREME -> group.extreme
                                        else -> group.mild
                                    }
                                    "Current" to event
                                } else {
                                    val event = when (WeatherAPI.nextIntensity) {
                                        WeatherIntensity.EXTREME -> group.extreme
                                        else -> group.mild
                                    }
                                    "Next" to event
                                }

                                display(Displays.text(Text.of(title)))
                                spacer(height = 6)
                                widget(
                                    Displays.text(Text.of(event.type.icon.toString(), event.type.color))
                                        .withTooltip(buildBonusTooltip(event))
                                        .asButtonLeft { onButtonPress(group, event) },
                                )
                                spacer(height = 6)
                                display(Displays.text(Text.of(event.type.weatherName, TextColor.WHITE)))

                                spacer(height = 12)

                                horizontal(alignment = MIDDLE) {
                                    display(Displays.text(Text.of("Mild", WeatherIntensity.MILD.color)))
                                    spacer(width = 12)
                                    display(Displays.text(Text.of("Ext", WeatherIntensity.EXTREME.color)))
                                }

                                spacer(height = 1)

                                horizontal(alignment = MIDDLE) {
                                    widget(
                                        Displays.text(Text.of(group.mild.type.icon.toString(), group.mild.type.color))
                                            .withTooltip(buildBonusTooltip(group.mild))
                                            .asButtonLeft { onButtonPress(group, group.mild) },
                                    )
                                    spacer(width = 20)
                                    widget(
                                        Displays.text(Text.of(group.extreme.type.icon.toString(), group.extreme.type.color))
                                            .withTooltip(buildBonusTooltip(group.extreme))
                                            .asButtonLeft { onButtonPress(group, group.extreme) },
                                    )
                                }
                            }
                        }.add()
                        spacer(width = 4)
                    }
                }
                spacer(height = 10)
            }
        }.asScrollable(listWidth, listHeight).add {
            alignHorizontallyCenter()
        }
    }

    private fun onButtonPress(group: WeatherGroup, event: WeatherEvent) {
        val enabled = WeatherAlertStorage.toggleAlert(group, event.intensity)
        if (enabled) {
            Text.of("Added reminder for ${group.island} ${event.intensity.displayName}!", TextColor.GREEN).sendWithPrefix()
        } else {
            Text.of("Removed reminder for ${group.island} ${event.intensity.displayName}.", TextColor.RED).sendWithPrefix()
        }
    }

    private fun VerticalLayoutBuilder.addFooter(frameWidth: Int) {
        LayoutFactory.frame(frameWidth, 20) {
            display(background(SkyOcean.id("hotkey/background"), frameWidth, 20))

            button {
                setSize(frameWidth - 20, 20)
                withTexture(null)
                withRenderer { graphics, widget, ticks ->
                    val regionName = WeatherGroup.getCurrentGroup()?.island?.toString() ?: "None"
                    val isActive = WeatherAPI.isActive
                    val intensity = WeatherAPI.currentIntensity?.component ?: Text.of("None")
                    val event = WeatherAPI.currentEvent?.type?.component ?: Text.of("Clear")

                    val nextIntensity = WeatherAPI.nextIntensity
                    val nextWeatherInstant = WeatherAPI.nextWeatherAt

                    val text = Text.of {
                        color = TextColor.WHITE
                        append("Region: ", TextColor.GRAY)
                        append(regionName)
                        append("   Now: ", TextColor.GRAY)
                        if (isActive) {
                            append(intensity)
                            append(" (")
                            append(event)
                            append(")")
                        } else {
                            append("Clear", TextColor.WHITE)
                        }

                        append("   Next: ", TextColor.GRAY)
                        if (nextIntensity != null && nextWeatherInstant != null) {
                            append(nextIntensity.component)
                            append(" in ${nextWeatherInstant.instant.until().toReadableTime()}")
                        } else {
                            append("N/A", TextColor.RED)
                        }
                    }

                    WidgetRenderers.text<Button>(text).apply { withShadow() }.render(graphics, widget, ticks)
                }
            }
        }.add()
    }

    private fun buildBonusTooltip(event: WeatherEvent): Component {
        return Text.multiline(
            buildList {
                add(event.type.component)
                add(Text.of("${event.intensity.displayName} Weather", TextColor.DARK_GRAY))
                add(CommonText.EMPTY)
                add(
                    Text.of {
                        append("During ", TextColor.GRAY)
                        append(event.type.weatherName, event.type.color)
                        append(":", TextColor.GRAY)
                    },
                )

                event.bonuses.forEach { (stat, value) ->
                    val formattedValue = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
                    add(
                        Text.of {
                            append(" • ", TextColor.DARK_GRAY)
                            append("+$formattedValue ", TextColor.GREEN)
                            append(stat.toString())
                        },
                    )
                }

                event.specialEffect?.let {
                    add(
                        Text.of {
                            append(" • ", TextColor.DARK_GRAY)
                            append(it)
                        },
                    )
                }
                add(CommonText.EMPTY)
                add(Text.of("Click to toggle reminder!", TextColor.YELLOW))
            },
        )
    }

    @Module
    companion object {
        @Subscription
        fun onCommand(event: RegisterSkyOceanCommandEvent) {
            event.command("weather") {
                execute {
                    McClient.setScreenAsync { WeatherScreen() }
                }

                "reminder" {
                    "add group"(EnumArgument<WeatherGroup>()) {
                        "intensity"(EnumArgument<WeatherIntensity>()) {
                            execute { group, intensity ->
                                WeatherAlertStorage.addAlert(group, intensity)
                                Text.of("Added reminder for $group $intensity").sendWithPrefix()
                            }
                        }
                    }
                    "remove group"(EnumArgument<WeatherGroup>()) {
                        "intensity"(EnumArgument<WeatherIntensity>()) {
                            execute { group, intensity ->
                                WeatherAlertStorage.removeAlert(group, intensity)
                                Text.of("Removed reminder for $group $intensity").sendWithPrefix()
                            }
                        }
                    }
                }
            }
        }
    }
}
