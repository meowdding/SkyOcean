package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.profile.items.museum.MuseumAPI
import tech.thatgravyboat.skyblockapi.api.profile.items.museum.MuseumCategory
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object MuseumItemSource : ItemSource {
    override fun getAll() = MuseumAPI.getItemsWithCategory().flatMap { (category, items) ->
        items.map { SimpleTrackedItem(it, MuseumItemContext(category)) }
    }

    override val type = ItemSources.MUSEUM
}

// TODO: add highlighting categories and items in museum menu
data class MuseumItemContext(private val category: MuseumCategory) : ItemContext {
    override fun collectLines(): List<Component> = build {
        add("Museum Category $category") { color = TextColor.GRAY }
        add("Click to warp to museum!") { color = TextColor.YELLOW }
    }

    override fun open() = McClient.sendCommand("warp museum")

    override val source = ItemSources.MUSEUM
}
