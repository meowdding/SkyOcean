package me.owdding.skyblockpv.museum

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ItemParser : MuseumProcessor() {
    override fun process(item: JsonObject) {
        val id = item["id"].asString
        val museumData = item["museum_data"].asJsonObject

        val output = JsonObject()
        output["id"] = id

        if (museumData.has("parent")) {
            museumData["parent"].asJsonObject[id]?.let {
                output["parent"] = it
            }
        }

        if (museumData.has("mapped_item_ids")) {
            museumData["mapped_item_ids"]?.asJsonArray?.takeUnless { it.isEmpty }?.let {
                output["mapped_item_ids"] = it
            }
        }

        value.add(output)
    }

    override fun postProcess(): JsonElement {
        val output = JsonArray()

        value.forEach {
            if (it is JsonObject) {
                if (it.keySet().size == 1) {
                    output.add(it.get("id"))
                    return@forEach
                }
            }

            output.add(it)
        }

        return output
    }
}
