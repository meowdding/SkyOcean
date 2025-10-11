package me.owdding.skyocean.features.item.search

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object ItemSearch {

    val key = SkyOceanKeybind("item_search", InputConstants.KEY_O) keybind@{
        if (!LocationAPI.isOnSkyBlock) return@keybind
        McClient.setScreen(ItemSearchScreen)
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("search") {
            if (!LocationAPI.isOnSkyBlock) {
                Text.of("You must be on Skyblock!") { this.color = TextColor.RED }
                return@registerWithCallback
            }
            McClient.setScreen(ItemSearchScreen)
        }
    }

}
