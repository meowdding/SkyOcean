package me.owdding.skyocean.features.combat.slayer

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.combat.SlayerConfig
import me.owdding.skyocean.helpers.glowingColor
import me.owdding.skyocean.helpers.isGlowing
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
        "auric" to TextColor.YELLOW,
        "crystal" to TextColor.AQUA,
        "spirit" to TextColor.WHITE,
    ).toList()

    @Subscription(priority = Subscription.HIGH)
    @OnlySlayerType(SlayerType.INFERNO_DEMONLORD, acceptDemons = true)
    fun onBlazeSlayerLineChange(event: SlayerInfoLineChangeEvent) {
        if (!SlayerConfig.enableBlazeHighlight) {
            event.slayerInfo.entity.isGlowing = false
            return
        }

        val stripped = event.component.stripped

        val color = colors.firstOrNull { stripped.startsWith(it.first, true) }?.second ?: return

        event.slayerInfo.entity.isGlowing = true
        event.slayerInfo.entity.glowingColor = color
    }
}
