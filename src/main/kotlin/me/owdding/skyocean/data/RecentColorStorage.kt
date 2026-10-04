package me.owdding.skyocean.data

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.item.custom.data.ItemColor
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.extensions.truncate

@LateInitModule
object RecentColorStorage {

    internal val storage = SkyOcean.storage("recent_colors", { mutableListOf() }, CodecHelpers.mutableList<ItemColor>())

    fun getColorAt(index: Int) = storage.get().getOrNull(index)
    fun addColor(color: ItemColor) {
        val list = storage.get()
        list.remove(color)
        list.addFirst(color)
        list.truncate(12)
        storage.save()
    }

}
