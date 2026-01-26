package me.owdding.skyocean.features.item.search.search.tag

import me.owdding.skyocean.features.item.search.search.ItemFilter
import me.owdding.skyocean.features.item.search.search.NameLoreSearchFilter
import me.owdding.skyocean.features.item.search.search.tag.tags.DataTypeSearchTag
import me.owdding.skyocean.features.item.search.search.tag.tags.IdSearchTag
import me.owdding.skyocean.features.item.search.search.tag.tags.LoreSearchTag
import me.owdding.skyocean.features.item.search.search.tag.tags.MuseumDonatedSearchTag
import me.owdding.skyocean.features.item.search.search.tag.tags.NameSearchTag
import tech.thatgravyboat.skyblockapi.api.datatype.DataType
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.impl.DataTypesRegistry

enum class SearchTags(searchTag: SearchTag) : SearchTag by searchTag {
    DATA_TYPE(DataTypeSearchTag),
    ID(IdSearchTag),
    MUSEUM_DONATED(MuseumDonatedSearchTag),
    LORE(LoreSearchTag),
    NAME(NameSearchTag),
}

fun interface SearchTag {
    fun parse(reader: StringReader): ItemFilter

    companion object {
        fun getDataType(id: String): DataType<*>? {
            return DataTypesRegistry.types.find { it.id.equals(id, true) }
        }

        fun filterOf(dataType: DataType<*>, value: String): ItemFilter {
            val matcher = StringMatcher.of(value)
            return ItemFilter {
                it.getData(dataType)?.toString()?.let(matcher::matches) == true
            }
        }

        fun ofDataType(dataTypeId: String): SearchTag? {
            val dataType = DataTypesRegistry.types.find { it.id.equals(dataTypeId, true) } ?: return null
            return SearchTag {
                filterOf(dataType, it.readString())
            }
        }
    }
}

object SearchTagsParser {
    fun parse(string: String): Pair<ItemFilter, List<TagException>>? {
        val reader = StringReader(string)
        val string = StringBuilder()
        var expr: ItemFilter? = null

        val exceptions = mutableListOf<TagException>()

        while (reader.canRead()) {
            if (reader.peek() == '#') {
                reader.skip()
                expr = TagExpressions.parseTagExpr(reader, exceptions)
                break
            }
            string.append(reader.read())
        }

        val result = string.toString().trim()
        if (result.isEmpty()) return null

        var filter: ItemFilter = NameLoreSearchFilter(result)
        if (expr != null) filter = filter.and(expr)

        return filter to exceptions
    }

}


fun interface StringMatcher {
    fun matches(string: String): Boolean

    companion object {
        fun of(string: String, equals: Boolean = false): StringMatcher {
            if (string.startsWith("r:")) {
                val ignoreCase = string.first().isLowerCase()
                val options = if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()
                val regex = string.drop(2).toRegex(options)
                return StringMatcher(regex::containsMatchIn)
            }
            return if (equals) StringMatcher { it.equals(string, true) }
            else StringMatcher { it.contains(string, true) }
        }
    }
}

// We collect exceptions instead of throwing them so that we can still have a partial item filter result midway through
// writing.
object TagExpressions {

    val ALWAYS = ItemFilter { true }
    val NEVER = ItemFilter { false }

    fun parseTagExpr(reader: StringReader, exceptions: MutableList<TagException>): ItemFilter = parseOr(reader, exceptions)

    private fun parseOr(reader: StringReader, exceptions: MutableList<TagException>): ItemFilter {
        var expr = parseAnd(reader, exceptions)

        while (reader.canRead()) {
            reader.skipWhitespace()

            if (reader.canRead(2) && reader.peek() == '|' && reader.peek(1) == '|') {
                reader.skip(2)
                reader.skipWhitespace()

                if (!reader.canRead()) {
                    exceptions.add(TagException.missing("value after '&&'"))
                    break
                }
                val other = parseAnd(reader, exceptions)
                expr = expr.or(other)
            } else break
        }

        return expr
    }

    private fun parseAnd(reader: StringReader, exceptions: MutableList<TagException>): ItemFilter {
        var expr = parseUnary(reader, exceptions) ?: NEVER

        while (reader.canRead()) {
            reader.skipWhitespace()

            if (reader.canRead(2) && reader.peek() == '&' && reader.peek(1) == '&') {
                reader.skip(2)
            } else if (reader.canRead() && (reader.peek() == '(' || reader.peek().isLetter())) {
                // if you don't specify and there's just a space it treats it as an and
            } else break

            if (!reader.canRead()) {
                exceptions.add(TagException.missing("value after '||'"))
                break
            }
            val other = parseUnary(reader, exceptions) ?: break

            expr = expr.and(other)
        }

        return expr
    }

    private fun parseUnary(reader: StringReader, exceptions: MutableList<TagException>): ItemFilter? {
        reader.skipWhitespace()
        if (!reader.canRead()) return null

        return if (reader.peek() == '!') {
            reader.skip()
            parseUnary(reader, exceptions)?.negate()
        } else {
            parsePrimary(reader, exceptions)
        }
    }

    private fun parsePrimary(reader: StringReader, exceptions: MutableList<TagException>): ItemFilter? {
        reader.skipWhitespace()

        return when (reader.peek()) {
            '(' -> {
                reader.skip()
                val expr = parseTagExpr(reader, exceptions)
                reader.skipWhitespace()
                if (reader.canRead() && reader.peek() == ')') reader.skip()
                else exceptions.add(TagException.missing("closing bracket"))
                expr
            }
            else -> parseTagLeaf(reader, exceptions)
        }
    }

    private fun parseTagLeaf(reader: StringReader, exceptions: MutableList<TagException>): ItemFilter? {
        val tagName = runCatching {
            reader.readStringUntil('=')
        }.onFailure {
            if (it is TagException) exceptions.add(it)
        }.getOrNull() ?: return null

        if (!reader.canRead()) {
            exceptions.add(TagException.expected("value after '$tagName='"))
            return null
        }

        val tag = SearchTags.entries
            .firstOrNull { it.name.equals(tagName, true) }
            ?: SearchTag.ofDataType(tagName)
            ?: run {
                exceptions.add(TagException.invalid("search tag", tagName))
                return null
            }

        return runCatching {
            tag.parse(reader)
        }.onFailure {
            if (it is TagException) exceptions.add(it)
        }.getOrNull()

    }

}
