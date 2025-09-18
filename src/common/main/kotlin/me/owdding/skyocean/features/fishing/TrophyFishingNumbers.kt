package me.owdding.skyocean.features.fishing

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ordinal
import me.owdding.skyocean.config.features.fishing.FishingConfig
import me.owdding.skyocean.utils.chat.ChatUtils
import net.minecraft.network.chat.CommonComponents
import tech.thatgravyboat.skyblockapi.api.area.isle.trophyfish.TrophyFishTier
import tech.thatgravyboat.skyblockapi.api.area.isle.trophyfish.TrophyFishType
import tech.thatgravyboat.skyblockapi.api.area.isle.trophyfish.TrophyFishingAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.location.isle.TrophyFishCaughtEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object TrophyFishingNumbers {

    var lastFishCaught: TrophyFishType? = null
    var lastFishTier: TrophyFishTier? = null

    @Subscription
    @OnlyIn(SkyBlockIsland.CRIMSON_ISLE)
    fun catchThingy(event: TrophyFishCaughtEvent) {
        if (!FishingConfig.enableTrophyNumbers) return
        lastFishCaught = event.type
        lastFishTier = event.tier
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRIMSON_ISLE)
    fun modifyChatMessage(event: ChatReceivedEvent.Post) {
        if (!FishingConfig.enableTrophyNumbers) return
        if (!event.text.startsWith("♔ TROPHY FISH! You caught ")) return
        val lastCaught = lastFishCaught ?: return
        val lastTier = lastFishTier ?: return
        event.component = Text.of {
            append(ChatUtils.ICON_SPACE_COMPONENT)
            append("♔ TROPHY FISH!") {
                this.bold = true
                this.color = TextColor.GOLD
            }
            append(" You caught your ")
            val caughtFishies = TrophyFishingAPI.getCaught(lastCaught)
            val caughtAmount = caughtFishies[lastTier] ?: 1
            append(caughtAmount.toFormattedString()) {
                append(caughtAmount.ordinal())
            }
            append(CommonComponents.SPACE)
            append(lastCaught.displayName)
            append(CommonComponents.SPACE)
            append(lastTier.displayName)
            append("!")
            append(CommonComponents.SPACE)
            append("(") {
                val caught = caughtFishies.values.sum()
                append(caught.toFormattedString())
                append(caught.ordinal())
                this.color = TextColor.GRAY
                append(")")
            }

            this.color = TextColor.WHITE
        }
    }

}
