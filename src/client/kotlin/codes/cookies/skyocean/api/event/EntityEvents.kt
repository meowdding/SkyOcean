package codes.cookies.skyocean.api.event

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.CancellableSkyBlockEvent
import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription

internal interface ListenForNameChange {

    fun `ocaen$markAsNameTag`()
    fun `ocean$unmarkNameTag`()
    fun `ocean$isNameTag`(): Boolean

}

object EntityEvents {

    @Subscription(priority = Subscription.HIGHEST)
    fun onNameAttach(event: ComponentAttachEvent) {
        event.component ?: return

        val literalContent = event.component.string
        if (literalContent.trim().startsWith("[Lv")) {
            event.cancel()
            EntityInfoLineAttachEvent(event.component, event.entity).post(SkyBlockAPI.eventBus)
            return
        }

        if (literalContent.startsWith("☠") && (literalContent.endsWith("❤") || literalContent.endsWith("❤ ✯"))) {
            SlayerInfoLineAttachEvent(event.component, event.entity).post(SkyBlockAPI.eventBus)
        }
    }

}

data class SlayerInfoLineAttachEvent(val component: Component, val entity: Entity) : SkyBlockEvent()
data class EntityInfoLineAttachEvent(val component: Component, val entity: Entity) : SkyBlockEvent()
data class ComponentAttachEvent(val component: Component?, val entity: Entity) : CancellableSkyBlockEvent()
data class NameChangedEvent(val entity: Entity, val component: Component) : SkyBlockEvent()