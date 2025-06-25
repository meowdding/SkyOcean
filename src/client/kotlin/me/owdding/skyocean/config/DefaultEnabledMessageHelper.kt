package me.owdding.skyocean.config

import com.mojang.serialization.Codec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.utils.storage.DataStorage

@Module
object DefaultEnabledMessageHelper {

    private val storage = DataStorage(
        0,
        { mutableListOf() },
        "default_enabled_messages",
        {
            CodecUtils.mutableList(Codec.STRING)
        },
    )

    fun needsSend(id: String): Boolean = !storage.get().contains(id)
    fun markSend(id: String) {
        storage.get().add(id)
        storage.save()
    }

}
