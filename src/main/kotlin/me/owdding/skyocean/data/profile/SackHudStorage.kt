package me.owdding.skyocean.data.profile

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.CodecUtils
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

object SackHudStorage {

    private val storage = SkyOcean.profileStorage(
        fileName = "sack_hud",
        defaultData = { mutableSetOf() },
        codec = CodecUtils.mutableSet(SkyBlockId.CODEC),
    )

    private val data get() = storage.get()
    val items: Set<SkyBlockId> get() = data?.toSet().orEmpty()

    fun add(id: SkyBlockId) {
        val data = data ?: return
        if (data.add(id)) save()
    }

    fun remove(id: SkyBlockId) {
        val data = data ?: return
        if (data.remove(id)) save()
    }

    fun clear() {
        val data = data ?: return
        if (data.isEmpty()) return
        data.clear()
        save()
    }

    private fun save() = storage.save()
}
