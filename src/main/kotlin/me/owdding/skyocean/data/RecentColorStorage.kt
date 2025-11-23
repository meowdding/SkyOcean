package me.owdding.skyocean.data

import me.owdding.ktmodules.Module
import me.owdding.skyocean.features.item.custom.data.ItemColor
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.extensions.truncate
import me.owdding.skyocean.utils.storage.DataStorage

@Module
object RecentColorStorage {

    internal val storage: DataStorage<MutableList<ItemColor>> = DataStorage({ mutableListOf() }, "recent_colors", CodecHelpers.list())

    fun getColorAt(index: Int) = storage.get().getOrNull(index)
    fun addColor(color: ItemColor) {
        val list = storage.get()
        list.remove(color)
        list.addFirst(color)
        list.truncate(12)
        storage.save()
    }

}
