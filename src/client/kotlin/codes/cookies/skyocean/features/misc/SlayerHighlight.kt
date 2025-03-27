package codes.cookies.skyocean.features.misc

import codes.cookies.skyocean.api.event.SlayerInfoLineAttachEvent
import net.minecraft.world.entity.Entity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent

object SlayerHighlight {

    class SlayerInfo

    val slayerBosses: MutableMap<Entity, SlayerInfo> = mutableMapOf()


    @TimePassed("5t")
    @Subscription
    fun tick(event: TickEvent) {

    }

    @Subscription
    fun onSlayerBarUpdate(event: SlayerInfoLineAttachEvent) {
        slayerBosses.put(event.entity, SlayerInfo())
    }
}
