package me.owdding.skyblockpv.museum

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ArmorParser : MuseumProcessor() {
    val armorMap: MutableMap<String, MutableList<String>> = mutableMapOf()

    override fun process(item: JsonObject) {
        val id = item["id"].asString
        val museumData = item["museum_data"].asJsonObject

        val output = JsonObject()
        output["id"] = id

        if (armorMap.containsKey(id)) {
            val collect = JsonArray().apply {
                armorMap[id]?.forEach { add(it) }
            }
            output["armor_ids"] = collect
        } else if (museumData.has("armor_set_donation_xp")) {
            val amorSetDonationXp = museumData["armor_set_donation_xp"].asJsonObject

            val armorIds = JsonArray()
            amorSetDonationXp.keySet().forEach { armorIds.add(it) }

            output["armor_ids"] = armorIds
        }
        if (museumData.has("parent")) {
            output["parent"] = museumData["parent"].asJsonObject.keySet().firstOrNull()
        }

        value.add(output)
    }

    override fun postProcess(): JsonElement {
        val armorMap = mutableMapOf<String, JsonObject>()

        value.filterIsInstance<JsonObject>().forEach { armor ->
            val armorIds = armor["armor_ids"].asJsonArray
            armorIds.map { it.asString }.forEach { armorId ->
                val armorObject = armorMap.getOrPut(armorId, ::JsonObject)

                armorObject["armor_id"] = armorId
                armorObject["parent_id"] = armor["parent"]
                val items = armorObject["items"]?.asJsonArray ?: JsonArray()
                items.add(armor["id"])
                armorObject["items"] = items
            }
        }

        return JsonArray().apply {
            armorMap.values.forEach { add(it) }
        }
    }
}
