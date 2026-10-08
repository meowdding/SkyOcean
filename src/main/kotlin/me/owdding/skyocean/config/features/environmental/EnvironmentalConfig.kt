package me.owdding.skyocean.config.features.environmental

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.defaultEnabledMessage
import me.owdding.skyocean.config.translation
import me.owdding.skyocean.features.environmental.WeatherScreen
import me.owdding.skyocean.utils.Utils.unaryPlus
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.CRYSTAL_HOLLOWS
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.Companion.inAnyIsland
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.DUNGEON_HUB
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.DWARVEN_MINES
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.KUUDRA
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.MINESHAFT
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.THE_CATACOMBS
import tech.thatgravyboat.skyblockapi.helpers.McClient

object EnvironmentalConfig : CategoryKt("environmental") {
    override val name get() = Translated("skyocean.config.environmental")
    override val baseTranslation: String get() = "skyocean.config.environmental"
    private val defaultCloudIslands = listOf(DWARVEN_MINES, CRYSTAL_HOLLOWS, MINESHAFT, THE_CATACOMBS, DUNGEON_HUB, KUUDRA)

    var hideLightning by boolean(false) {
        translation = "hideLightning"
    }

    var islandCloudHider by defaultEnabledMessage(
        select(*defaultCloudIslands.toTypedArray()) {
            translation = "islandCloudHider"
        },
        { +"skyocean.config.misc.islandCloudHider.warning" },
        "islandCloudHider",
        predicate = { inAnyIsland(defaultCloudIslands) },
    )
    val shouldHideClouds get() = SkyBlockIsland.inAnyIsland(islandCloudHider.toList())

    var netherFogDarkening by defaultEnabledMessage(
        boolean(true) {
            translation = "netherFogDarkening"
        },
        { +"skyocean.config.misc.netherFogDarkening.warning" }, "netherFogDarkening",
        predicate = { SkyBlockIsland.CRIMSON_ISLE.inIsland() },
    )

    var netherFogScale by float(0.25f) {
        translation = "netherFogScale"
        slider = true
        range = 0f..1f
    }

    init {
        separator {
            this.translation = "$baseTranslation.weather"
        }

        button {
            this.text = "weather.button.text"
            this.title = "weather.button.title"
            this.description = "weather.button.desc"
            onClick {
                McClient.setScreenAsync { WeatherScreen() }
            }
        }
    }

    var alertOtherIslands by boolean(false) {
        this.translation = "weather.alert_other_islands"
    }

    var weatherTitleAlert by boolean(true) {
        this.translation = "weather.weather_title_alert"
    }


}
