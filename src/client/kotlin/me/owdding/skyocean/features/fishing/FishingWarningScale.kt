package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.fishing.FishingConfig
import me.owdding.skyocean.helpers.nameTagScale
import net.minecraft.world.entity.LivingEntity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.entity.NameChangedEvent
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object FishingWarningScale {

    private val hookWarningRegex = Regex("^\\d+\\.\\d+|!+$")

    @Subscription
    @OnlyOnSkyBlock
    fun onEntityNameChange(event: NameChangedEvent) {
        if (!event.component.stripped.matches(hookWarningRegex)) return
        if (event.attachedTo != McPlayer.self?.fishing) return
        val armorStand = event.infoLineEntity as? LivingEntity ?: return
        armorStand.nameTagScale = FishingConfig.hookTextScale
    }
}
