package me.owdding.skyocean.features.item.search.search.tag

class TagException(message: String) : Exception(message) {
    companion object {
        fun invalid(string: String, value: Any): TagException = TagException("Invalid $string '$value'")
        fun expected(string: String): TagException = TagException("Expected $string")
        fun missing(string: String): TagException = TagException("Missing $string")
    }
}
