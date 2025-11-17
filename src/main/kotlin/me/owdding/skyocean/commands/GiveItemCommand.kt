package me.owdding.skyocean.commands

import com.google.gson.JsonParser
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow

@Module
object GiveItemCommand {

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDevWithCallback("give") {
            if (McPlayer.self?.gameMode()?.isCreative != true && McClient.self.isSingleplayer) return@registerDevWithCallback
            val item = JsonParser.parseString(McClient.clipboard).toDataOrThrow(ItemStack.CODEC)
            McClient.self.player?.inventory?.add(item)
        }
    }
}
