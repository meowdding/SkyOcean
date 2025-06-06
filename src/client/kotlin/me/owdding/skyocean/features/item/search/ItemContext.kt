package me.owdding.skyocean.features.item.search

import me.owdding.skyocean.features.item.search.soures.ItemSources
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.profile.effects.EffectsAPI
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.text.Text

interface ItemContext {

    fun open() {}
    fun collectLines(): List<Component>
    val source: ItemSources

    fun build(init: TooltipBuilder.() -> Unit): List<Component> {
        val builder = TooltipBuilder()
        builder.init()
        return builder.lines()
    }

    fun requiresCookie(runnable: () -> Unit) {
        if (!EffectsAPI.isBoosterCookieActive) {
            Text.of("Requires cookie!").sendWithPrefix()
            return
        }
        runnable()
    }
}
