package me.owdding.skyocean.features.item.sources.system

import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.profile.effects.EffectsAPI
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

interface ItemContext {

    fun open() {}
    fun collectLines(): List<Component>
    val source: ItemSources

    fun build(init: TooltipBuilder.() -> Unit): List<Component> {
        val builder = TooltipBuilder()
        builder.init()
        return builder.lines()
    }

    fun TooltipBuilder.riftWarning() {
        requiresRift { add("Not currently in overworld!") { color = TextColor.RED } }
    }

    fun requiresCookie(runnable: () -> Unit) {
        if (!EffectsAPI.isBoosterCookieActive) {
            Text.of("Requires a booster cookie!").sendWithPrefix()
            return
        }
        runnable()
    }

    fun requiresOverworld(sendMessage: Boolean = false, runnable: () -> Unit) {
        if (SkyBlockIsland.THE_RIFT.inIsland()) {
            if (sendMessage) Text.of("Requires not to be in the rift!").sendWithPrefix()
            return
        }
        runnable()
    }

    fun requiresRift(sendMessage: Boolean = false, runnable: () -> Unit) {
        if (!SkyBlockIsland.THE_RIFT.inIsland()) {
            if (sendMessage) Text.of("Requires to be in the rift!").sendWithPrefix()
            return
        }
        runnable()
    }
}

abstract class ParentItemContext(open val parent: SimpleTrackedItem) : ItemContext
