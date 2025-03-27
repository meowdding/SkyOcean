package codes.cookies.skyocean.helper

import codes.cookies.skyocean.api.event.NameChangedEvent
import codes.cookies.skyocean.helper.EntityAttachmentAccessor.Companion.asAccessor
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock


object SbEntity {
    @OnlyOnSkyBlock
    @Subscription
    fun event(event: NameChangedEvent) {
        event.entity.asAccessor().`ocean$attachToClosest`()
    }

    @JvmStatic
    fun getAttachedLines(entity: Entity): List<Component> = entity.getAttachedLines()
    @JvmStatic
    fun getAttachedEntities(entity: Entity): List<Entity> = entity.asAccessor().`ocean$getAttachments`()
}

internal interface EntityAttachmentAccessor {

    fun `ocean$attachToClosest`()
    fun `ocean$getAttachments`(): List<Entity>

    companion object {
        fun Entity.asAccessor(): EntityAttachmentAccessor {
            return this as EntityAttachmentAccessor
        }
    }

}

fun Entity.getMobLevel(): Int? {
    return this.getAttachedLines().mapNotNull {
        it.string.replace(Regex(".*(\\[Lv\\d+]).*"), "$1").takeIf(String::isNotBlank)
    }.map { it.replace(Regex("\\D"), "") }.map { it.toInt() }.firstOrNull()
}

fun Entity.getAttachedLines(): List<Component> = this.asAccessor().`ocean$getAttachments`().mapNotNull { it.customName }