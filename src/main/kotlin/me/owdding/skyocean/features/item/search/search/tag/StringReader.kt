package me.owdding.skyocean.features.item.search.search.tag

// Taken from Mojang's Brigadier StringReader class, converted to kotlin and slightly modified
class StringReader {
    val string: String
    var cursor = 0

    constructor(other: StringReader) {
        this.string = other.string
        this.cursor = other.cursor
    }

    constructor(string: String) {
        this.string = string
    }

    val remainingLength: Int get() = totalLength - cursor

    val totalLength: Int get() = string.length

    val read: String
        get() = string.substring(0, cursor)

    val remaining: String
        get() = string.substring(cursor)

    fun canRead(length: Int): Boolean = cursor + length <= string.length

    fun canRead(): Boolean = canRead(1)

    fun peek(): Char = string[cursor]

    fun peek(offset: Int): Char = string[cursor + offset]

    fun read(): Char = string[cursor++]

    fun skip() = skip(1)

    fun skip(amount: Int) {
        cursor += amount
    }

    fun skipUntilNot(predicate: (Char) -> Boolean) {
        while (canRead() && predicate(peek())) skip()
    }

    fun skipWhitespace() = skipUntilNot(Character::isWhitespace)

    @Throws(TagException::class)
    fun readInt(): Int {
        val start = cursor
        while (canRead() && isAllowedNumber(peek())) {
            skip()
        }
        val number = string.substring(start, cursor)
        if (number.isEmpty()) throw TagException.expected("integer")
        try {
            return number.toInt()
        } catch (_: NumberFormatException) {
            cursor = start
            throw TagException.invalid("integer", number)
        }
    }

    @Throws(TagException::class)
    fun readLong(): Long {
        val start = cursor
        while (canRead() && isAllowedNumber(peek())) {
            skip()
        }
        val number = string.substring(start, cursor)
        if (number.isEmpty()) throw TagException.expected("long")
        try {
            return number.toLong()
        } catch (_: NumberFormatException) {
            cursor = start
            throw TagException.invalid("long", number)
        }
    }

    @Throws(TagException::class)
    fun readDouble(): Double {
        val start = cursor
        while (canRead() && isAllowedNumber(peek())) {
            skip()
        }
        val number = string.substring(start, cursor)
        if (number.isEmpty()) throw TagException.expected("double")
        try {
            return number.toDouble()
        } catch (_: NumberFormatException) {
            cursor = start
            throw TagException.invalid("double", number)
        }
    }

    @Throws(TagException::class)
    fun readFloat(): Float {
        val start = cursor
        while (canRead() && isAllowedNumber(peek())) {
            skip()
        }
        val number = string.substring(start, cursor)
        if (number.isEmpty()) throw TagException.expected("float")
        try {
            return number.toFloat()
        } catch (_: NumberFormatException) {
            cursor = start
            throw TagException.invalid("float", number)
        }
    }

    fun readUnquotedString(): String {
        val start = cursor
        while (canRead() && isAllowedInUnquotedString(peek())) {
            skip()
        }
        return string.substring(start, cursor)
    }

    @Throws(TagException::class)
    fun readQuotedString(): String {
        if (!canRead()) return ""
        val next = peek()
        if (!isQuotedStringStart(next)) {
            throw TagException("Expected quote to start a string")
        }
        skip()
        return readStringUntil(next)
    }

    @Throws(TagException::class)
    fun readStringUntil(vararg chars: Char): String {
        val result = StringBuilder()
        var escaped = false
        while (canRead()) {
            val c = read()
            if (escaped) {
                if (c in chars || c == SYNTAX_ESCAPE) {
                    result.append(c)
                }
                escaped = false
            } else if (c == SYNTAX_ESCAPE) {
                escaped = true
            } else if (c in chars) {
                return result.toString()
            } else {
                result.append(c)
            }
        }

        if (escaped) throw TagException("Unclosed quoted string")
        else {
            val msg = buildString {
                append("Didn't find ")
                if (chars.size == 1) append("char '${chars.first()}'")
                else append("one of '${chars.joinToString()}' chars")
                append("in string")
            }
            throw TagException(msg)
        }
    }

    @Throws(TagException::class)
    fun readString(): String {
        if (!canRead()) return ""
        val next = peek()
        if (isQuotedStringStart(next)) {
            skip()
            return readStringUntil(next)
        }
        return readUnquotedString()
    }

    @Throws(TagException::class)
    fun readBoolean(): Boolean {
        val start = cursor
        val value = readString()
        if (value.isEmpty()) throw TagException.expected("bool")

        return when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> {
                cursor = start
                throw TagException.invalid("bool", value)
            }
        }
    }

    @Throws(TagException::class)
    fun expect(c: Char) {
        if (!canRead() || peek() != c) {
            throw TagException.expected("'$c'")
        }
        skip()
    }

    companion object {
        private const val SYNTAX_ESCAPE = '\\'
        private const val SYNTAX_DOUBLE_QUOTE = '"'
        private const val SYNTAX_SINGLE_QUOTE = '\''

        fun isAllowedNumber(c: Char): Boolean {
            return c in '0'..'9' || c == '.' || c == '-'
        }

        fun isQuotedStringStart(c: Char): Boolean {
            return c == SYNTAX_DOUBLE_QUOTE || c == SYNTAX_SINGLE_QUOTE
        }

        fun isAllowedInUnquotedString(c: Char): Boolean {
            return c in '0'..'9' || c in 'A'..'Z' || c in 'a'..'z' || c == '_' || c == '-' || c == '.' || c == '+'
        }
    }
}
