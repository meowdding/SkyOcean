package codes.cookies.skyocean

import codes.cookies.skyocean.api.event.EntityEvents
import codes.cookies.skyocean.features.misc.SlayerHighlight
import codes.cookies.skyocean.helper.SbEntity
import codes.cookies.skyocean.utils.ChatUtils
import net.fabricmc.api.ClientModInitializer
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text.send

object SkyOcean : ClientModInitializer {
    override fun onInitializeClient() {
        SkyBlockAPI.eventBus.register(SbEntity)
        SkyBlockAPI.eventBus.register(EntityEvents)
        SkyBlockAPI.eventBus.register(SlayerHighlight)
        SkyBlockAPI.eventBus.register(SkyOcean)
    }


    @Subscription
    fun commands(event: RegisterCommandsEvent) {
        event.register("skyocean") {
            this.callback {
                ChatUtils.prefix.send()
            }
        }
    }
}
