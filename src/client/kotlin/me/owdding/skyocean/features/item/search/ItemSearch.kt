package me.owdding.skyocean.features.item.search

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen
import org.lwjgl.glfw.GLFW
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient

@Module
object ItemSearch {

    val key = SkyOceanKeybind("skyocean.item_search.keybind", GLFW.GLFW_KEY_O) {
        McClient.setScreen(ItemSearchScreen)
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("search") {
            McClient.setScreen(ItemSearchScreen)
        }
    }

}
