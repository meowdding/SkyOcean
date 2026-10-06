package me.owdding.skyocean.config.features.environmental

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.translation
import me.owdding.skyocean.features.environmental.WeatherScreen
import tech.thatgravyboat.skyblockapi.helpers.McClient

object EnvironmentalConfig : CategoryKt("environmental") {
    override val name get() = Translated("skyocean.config.environmental")
    override val baseTranslation: String get() = "skyocean.config.environmental"

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
        this.translation = "alert_other_islands"
    }

    var weatherTitleAlert by boolean(true) {
        this.translation = "$baseTranslation.weather_title_alert"
    }


}
