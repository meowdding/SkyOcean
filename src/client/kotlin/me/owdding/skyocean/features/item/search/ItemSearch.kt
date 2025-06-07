package me.owdding.skyocean.features.item.search

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient

@Module
object ItemSearch {

    val key = SkyOceanKeybind("skyocean.item_search.keybind", InputConstants.KEY_O) keybind@{
        if (!LocationAPI.isOnSkyBlock) return@keybind
        McClient.setScreen(ItemSearchScreen)
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("search") {
            McClient.setScreen(ItemSearchScreen)
        }
    }

}
