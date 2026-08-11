package me.owdding.skyocean.config

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.codecs.CodecHelpers

@Module
object DefaultEnabledMessageHelper {

    private val storage = SkyOcean.storage(
        "default_enabled_messages",
        { mutableListOf() },
        CodecHelpers.mutableList<String>()
    )

    fun needsSend(id: String): Boolean = !storage.get().contains(id)
    fun markSend(id: String) {
        storage.get().add(id)
        storage.save()
    }

}
