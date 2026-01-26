package me.owdding.skyocean.features.item.search.search.tag.tags

import me.owdding.skyocean.features.item.search.search.ItemFilter
import me.owdding.skyocean.features.item.search.search.tag.SearchTag
import me.owdding.skyocean.features.item.search.search.tag.StringReader
import tech.thatgravyboat.skyblockapi.api.profile.items.museum.MuseumAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId

object MuseumDonatedSearchTag : SearchTag {
    override fun parse(reader: StringReader): ItemFilter {
        val value = reader.readBoolean()
        return ItemFilter {
            val id = it.getSkyBlockId() ?: return@ItemFilter false
            if (!MuseumAPI.isMuseumItem(id)) return@ItemFilter false
            MuseumAPI.isDonated(id) == value
        }
    }
}
