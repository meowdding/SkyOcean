package me.owdding.skyocean.features.item.search.search.tag.tags

import me.owdding.skyocean.features.item.search.search.ItemFilter
import me.owdding.skyocean.features.item.search.search.tag.SearchTag
import me.owdding.skyocean.features.item.search.search.tag.StringReader
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId

object IdSearchTag : SearchTag {
    override fun parse(reader: StringReader): ItemFilter {
        val searchedId = reader.readString()
        return ItemFilter {
            val id = it.getSkyBlockId() ?: return@ItemFilter false
            id.skyblockId.contains(searchedId, true)
        }
    }
}
