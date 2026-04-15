package me.owdding.skyocean.data.profile

import com.mojang.serialization.Codec
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.utils.storage.ProfileStorage


private typealias FamilyName = String

private typealias Tier = Int

object MarkedAccessoriesStorage {

    private val storage = ProfileStorage(
        defaultData = ::LinkedHashMap,
        fileName = "marked_accessories",
        codec = { CodecUtils.map(Codec.STRING, Codec.INT) },
    )

    private val data get() = storage.get()

    fun get(): Map<FamilyName, Tier> = data.orEmpty()

    fun add(family: FamilyName, tier: Tier) {
        val data = data ?: return
        if (data.put(family, tier) != tier) save()
    }

    fun remove(accessory: FamilyName, tier: Tier) {
        val data = data ?: return
        if (data.remove(accessory, tier)) save()
    }

    fun track(family: FamilyName, tier: Tier, track: Boolean) {
        if (track) add(family, tier) else remove(family, tier)
    }

    fun has(family: FamilyName, tier: Tier): Boolean {
        val data = data ?: return false
        return data[family] == tier
    }

    fun clear() {
        val data = data ?: return
        if (data.isEmpty()) return
        data.clear()
        save()
    }

    private fun save() = storage.save()

}
