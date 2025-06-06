package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import me.owdding.skyocean.utils.Utils.mapNotNull
import tech.thatgravyboat.skyblockapi.api.profile.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient

object SacksItemSource : ItemSource {
    override fun getAll() = SacksAPI.sackItems.mapNotNull(
        { (id) -> SkyOcean.warn("Couldn't find item for {}", id) },
        { (id, amount) -> createFromIdAndAmount(id, amount) },
    ).map { SimpleTrackedItem(it, SackItemContext) }

    override fun remove(item: SimpleTrackedItem) {
        TODO("Not yet implemented")
    }

    override val type = ItemSources.SACKS
}

object SackItemContext : ItemContext {

    override val source = ItemSources.SACKS

    override fun collectLines() = build {
        add("Sacks :3")
    }

    override fun open() = McClient.sendCommand("sacks")
}
