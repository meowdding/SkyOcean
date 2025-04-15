package codes.cookies.skyocean.features.recipe

import codes.cookies.skyocean.events.RegisterSkyOceanCommandEvent
import codes.cookies.skyocean.helpers.ClientSideInventory
import codes.cookies.skyocean.modules.Module
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text

class ForgeRecipeScreen : ClientSideInventory(Text.of("Forge"), 6) {
    init {
        val items = List(6 * 9) { Items.BARRIER.defaultInstance }
        addItems(items)
    }

    @Module
    companion object {
        @Subscription
        fun onCommand(event: RegisterSkyOceanCommandEvent) {
            event.register("forge") {
                callback {
                    McClient.setScreen(ForgeRecipeScreen())
                }
            }
        }
    }
}
