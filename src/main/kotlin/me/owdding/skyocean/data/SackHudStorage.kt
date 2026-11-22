package me.owdding.skyocean.data

import com.mojang.serialization.Codec
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.CodecUtils

object SackHudStorage {

    private val storage = SkyOcean.storage(
        "sack_hud",
        defaultData = { mutableListOf() },
        codec = CodecUtils.mutableList(Codec.STRING),
        differentAlphaData = false,
    )

    var items: List<String>
        get() = storage.get().toList()
        set(value) {
            storage.get().apply {
                clear()
                addAll(value)
            }
            storage.save()
        }
}
