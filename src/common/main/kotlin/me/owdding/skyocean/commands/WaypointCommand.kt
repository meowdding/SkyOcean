package me.owdding.skyocean.commands

import com.google.gson.JsonElement
import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.misc.waypoints.OrderedWaypointData
import me.owdding.skyocean.features.misc.waypoints.WaypointManager
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils.getArgument
import me.owdding.skyocean.utils.Utils.text
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow

@Module
object WaypointCommand {

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("waypoints ordered") {
            thenCallback("name", StringArgumentType.word()) {
                val clipboard = McClient.clipboard
                val data = clipboard.readJson<JsonElement>().toDataOrThrow(SkyOceanCodecs.getCodec<OrderedWaypointData>().listOf())
                WaypointManager.addOrdered(getArgument<String>("name")!!, data)
                text("Successfully loaded ${data.size} waypoints!   ")
            }
        }
    }

}
