package me.owdding.skyocean.features.misc.itemsearch

import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder

interface ItemContext {

    fun open() {}
    fun collectLines(): List<Component>

    fun build(init: TooltipBuilder.() -> Unit): List<Component> {
        val builder = TooltipBuilder()
        builder.init()
        return builder.lines()
    }

}
