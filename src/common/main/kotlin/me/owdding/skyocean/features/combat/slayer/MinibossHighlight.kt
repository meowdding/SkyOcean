package me.owdding.skyocean.features.combat.slayer

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.combat.SlayerConfig
import me.owdding.skyocean.helpers.glowingColor
import me.owdding.skyocean.helpers.isGlowing
import tech.thatgravyboat.skyblockapi.api.area.slayer.SlayerMiniBoss
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlySlayerMiniBosses
import tech.thatgravyboat.skyblockapi.api.events.entity.SlayerEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@Module
object MinibossHighlight {

    @Subscription
    @OnlySlayerMiniBosses
    fun onMiniBossEvent(event: SlayerEvent) {
        val type = event.slayerInfo.type ?: return
        if (type !is SlayerMiniBoss) {
            return
        }

        val entity = event.slayerInfo.entity
        if (!SlayerConfig.highlightMini) {
            entity.isGlowing = false
            return
        }

        entity.isGlowing = true
        if (SlayerConfig.highlightBigBoys && type.isBigBoy) {
            entity.glowingColor = TextColor.DARK_RED
        } else {
            entity.glowingColor = TextColor.RED
        }
    }

}
