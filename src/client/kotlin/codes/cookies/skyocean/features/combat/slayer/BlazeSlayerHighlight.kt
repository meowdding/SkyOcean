package codes.cookies.skyocean.features.combat.slayer

import codes.cookies.skyocean.config.features.combat.SlayerConfig
import codes.cookies.skyocean.helpers.glowingColor
import codes.cookies.skyocean.helpers.isGlowing
import codes.cookies.skyocean.modules.Module
import tech.thatgravyboat.skyblockapi.api.area.slayer.SlayerType
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlySlayerType
import tech.thatgravyboat.skyblockapi.api.events.entity.SlayerInfoLineChangeEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object BlazeSlayerHighlight {
    private val colors = mapOf(
        "immune" to TextColor.RED,
        "ashen" to TextColor.DARK_GRAY,
        "auric" to TextColor.GOLD,
        "crystal" to TextColor.AQUA,
        "spirit" to TextColor.WHITE
    ).toList()

    @Subscription
    @OnlySlayerType([SlayerType.INFERNO_DEMONLORD], acceptDemons = true)
    fun onBlazeSlayerLineChange(event: SlayerInfoLineChangeEvent) {
        if (!SlayerConfig.enableBlazeHighlight) {
            return
        }

        val stripped = event.component.stripped

        val color = colors.firstOrNull { stripped.startsWith(it.first, true) }?.second?: return

        event.slayerInfo.entity.isGlowing = true
        event.slayerInfo.entity.glowingColor = color
    }
}
