package me.owdding.skyocean.features.item.search.search.tag.tags

import me.owdding.skyocean.features.item.search.search.ItemFilter
import me.owdding.skyocean.features.item.search.search.tag.SearchTag
import me.owdding.skyocean.features.item.search.search.tag.StringMatcher
import me.owdding.skyocean.features.item.search.search.tag.StringReader
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore

object LoreSearchTag : SearchTag {
    override fun parse(reader: StringReader): ItemFilter {
        val matcher = StringMatcher.of(reader.readString())
        return ItemFilter {
            it.getRawLore().joinToString("\n").let(matcher::matches)
        }
    }
}
