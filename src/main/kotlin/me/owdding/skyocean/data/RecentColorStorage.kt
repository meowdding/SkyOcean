package me.owdding.skyocean.data

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.item.custom.data.ItemColor
import me.owdding.skyocean.repo.customization.DyeData
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.extensions.truncate

@LateInitModule
object RecentColorStorage {

    private var loadFailed = false

    internal val storage by lazy {
        val delegate = CodecHelpers.mutableList<ItemColor>()
        val codec = object : Codec<MutableList<ItemColor>> {
            override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<MutableList<ItemColor>, T>> {
                return try {
                    delegate.decode(ops, input).also { if (it.error().isPresent) loadFailed = true }
                } catch (exception: RuntimeException) {
                    loadFailed = true
                    throw exception
                }
            }

            override fun <T> encode(input: MutableList<ItemColor>, ops: DynamicOps<T>, prefix: T): DataResult<T> =
                delegate.encode(input, ops, prefix)
        }
        SkyOcean.storage("recent_colors", { mutableListOf() }, codec)
    }

    fun getColorAt(index: Int) = if (DyeData.isLoaded) storage.get().getOrNull(index) else null
    fun addColor(color: ItemColor) {
        if (!DyeData.isLoaded) return
        val list = storage.get()
        // A failed decode supplies an empty fallback. Never save that fallback
        // over the user's original history.
        if (loadFailed) return
        list.remove(color)
        list.addFirst(color)
        list.truncate(12)
        storage.save()
    }

}
