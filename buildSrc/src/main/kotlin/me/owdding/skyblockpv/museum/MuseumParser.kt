package me.owdding.skyblockpv.museum

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

enum class MuseumParser(val processor: MuseumProcessor) {
    WEAPONS(ItemParser("weapons")),
    ARMOR_SETS(ArmorParser()),
    RARITIES(ItemParser("rarities")),
}

abstract class MuseumProcessor(val key: String) {
    val value: JsonArray = JsonArray()

    abstract fun process(item: JsonObject)

    open fun postProcess(): JsonElement = value

    protected operator fun JsonObject.set(key: String, value: String?) {
        if (value == null) {
            this.remove(key)
            return
        }
        this.addProperty(key, value)
    }
    protected operator fun JsonObject.set(key: String, value: JsonElement?) {
        if (value == null) {
            this.remove(key)
            return
        }
        this.add(key, value)
    }
}
