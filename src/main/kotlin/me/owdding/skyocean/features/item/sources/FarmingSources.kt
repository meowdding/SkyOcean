package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.data.profile.FarmingItemStorage
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import me.owdding.skyocean.utils.chat.CatppuccinColors
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color


object FarmingToolkitSource : ItemSource, ItemContext {
    override val type = ItemSources.FARMING_TOOLKIT
    override val source: ItemSources = ItemSources.HUNTING_TOOLKIT

    override fun getAll(): List<SimpleTrackedItem> = FarmingItemStorage.data?.toolkitItems?.map {
        SimpleTrackedItem(it, this)
    } ?: emptyList()


    override fun collectLines(): List<Component> = build {
        add("Contained in ") {
            append("Farming Toolkit", CatppuccinColors.Mocha.green)
            append("!")
            color = TextColor.GRAY
        }
    }

    override fun open() = requiresOverworld(true) {
        McClient.sendCommand("farmingtoolkit")
    }
}

