package me.owdding.skyocean.dev

import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.centerIn
import me.owdding.skyocean.DevModule
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@DevModule
object InventoryTestScreen : SkyOceanScreen() {

    @Subscription
    context(event: RegisterSkyOceanCommandEvent)
    private fun registerCommand() = event.command("dev") {
        "inventories" executes {
            McClient.setScreenAsync { InventoryTestScreen }
        }
    }

    override fun init() {
        super.init()

        val inventories = listOf(
            ExtraDisplays.inventoryBackground(1, 1, Displays.empty(24, 24)),
            ExtraDisplays.inventoryBackground(3, 1, Displays.empty(84, 24)),
            ExtraDisplays.inventoryBackground(3, 3, Displays.empty(84, 84)),
            ExtraDisplays.inventoryBackground(1, 1, Displays.empty(24, 24), ARGB.opaque(TextColor.RED)),
        )


        addRenderableOnly(Displays.table(inventories.chunked(2)).centerIn(width, height).asWidget())
    }


}
