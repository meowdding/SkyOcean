package me.owdding.skyocean.features.item.search.search.tag.tags

import me.owdding.skyocean.features.item.search.search.ItemFilter
import me.owdding.skyocean.features.item.search.search.tag.SearchTag
import me.owdding.skyocean.features.item.search.search.tag.StringReader
import me.owdding.skyocean.features.item.search.search.tag.TagExpressions

object DataTypeSearchTag : SearchTag {
    override fun parse(reader: StringReader): ItemFilter {
        val dataType = SearchTag.getDataType(reader.readStringUntil('@')) ?: return TagExpressions.NEVER
        val value = reader.readString()

        return SearchTag.filterOf(dataType, value)
    }
}
