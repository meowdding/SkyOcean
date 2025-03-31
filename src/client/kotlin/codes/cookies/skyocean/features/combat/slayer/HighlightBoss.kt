package codes.cookies.skyocean.features.combat.slayer

import codes.cookies.skyocean.config.features.combat.SlayerConfig
import codes.cookies.skyocean.helpers.glowingColor
import codes.cookies.skyocean.helpers.isGlowing
import codes.cookies.skyocean.modules.Module
import tech.thatgravyboat.skyblockapi.api.area.slayer.SlayerType
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlySlayerBosses
import tech.thatgravyboat.skyblockapi.api.events.entity.SlayerEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@Module
object HighlightBoss {
    private val colors = mapOf(
        SlayerType.INFERNO_DEMONLORD to TextColor.GOLD,
        SlayerType.VOIDGLOOM_SERAPH to TextColor.DARK_PURPLE,
        SlayerType.RIFTSTALKER_BLOODFIEND to TextColor.DARK_AQUA,
        SlayerType.TARANTULA_BROODFATHER to TextColor.DARK_RED,
        SlayerType.SVEN_PACKMASTER to TextColor.BLUE,
        SlayerType.REVENANT_HORROR to TextColor.DARK_GREEN
    )

    @Subscription
    @OnlySlayerBosses
    fun onBlazeSlayerLineChange(event: SlayerEvent) {
        if (event.slayerInfo.type == SlayerType.INFERNO_DEMONLORD && SlayerConfig.enableBlazeHighlight) {
            return
        }

        if (!SlayerConfig.highlightOwnBoss) {
            event.slayerInfo.entity.isGlowing = false
            return
        }

        event.slayerInfo.entity.isGlowing = true
        event.slayerInfo.entity.glowingColor = colors[event.slayerInfo.type] ?: TextColor.GRAY
    }

}
