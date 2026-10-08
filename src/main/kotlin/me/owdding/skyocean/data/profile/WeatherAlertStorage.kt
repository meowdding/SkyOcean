package me.owdding.skyocean.data.profile

import com.mojang.serialization.Codec
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.EnumCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import tech.thatgravyboat.skyblockapi.api.environmental.WeatherGroup
import tech.thatgravyboat.skyblockapi.api.environmental.WeatherIntensity

object WeatherAlertStorage {
    private val storage = SkyOcean.storage(
        "weather_alert",
        { mutableMapOf() },
        CodecUtils.map(
            SkyOceanCodecs.getCodec<WeatherGroup>(),
            CodecUtils.mutableSet(SkyOceanCodecs.getCodec<WeatherIntensity>())
        )
    )

    fun hasAlert(group: WeatherGroup, intensity: WeatherIntensity): Boolean = storage.get()[group]?.contains(intensity) == true

    fun toggleAlert(group: WeatherGroup, intensity: WeatherIntensity): Boolean {
        val set = storage.get().getOrPut(group) { mutableSetOf() }

        return if (set.contains(intensity)) {
            removeAlert(group, intensity)
            false
        } else {
            addAlert(group, intensity)
            true
        }
    }

    fun addAlert(group: WeatherGroup, intensity: WeatherIntensity) {
        val map = storage.get()
        map.getOrPut(group) { mutableSetOf() }.add(intensity)
        storage.save()
    }

    fun removeAlert(group: WeatherGroup, intensity: WeatherIntensity) {
        val map = storage.get()
        val set = map[group] ?: return

        set.remove(intensity)
        if (set.isEmpty()) {
            map.remove(group)
        }
        storage.save()
    }
}
