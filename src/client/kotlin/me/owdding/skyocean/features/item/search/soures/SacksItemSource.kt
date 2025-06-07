package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import me.owdding.skyocean.utils.Utils.mapNotNull
import tech.thatgravyboat.skyblockapi.api.profile.items.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object SacksItemSource : ItemSource {
    override fun getAll() = SacksAPI.sackItems.mapNotNull(
        { (id) -> SkyOcean.warn("Couldn't find item for {}", id) },
        { (id, amount) -> createFromIdAndAmount(id, amount) },
    ).map { SimpleTrackedItem(it, SackItemContext) }

    override val type = ItemSources.SACKS
}

object SackItemContext : ItemContext {

    override val source = ItemSources.SACKS

    override fun collectLines() = build {
        add("Sacks :3") { color = TextColor.GRAY }
        add("Click to open sacks!") { this.color = TextColor.YELLOW }
    }

    override fun open() = McClient.sendCommand("sacks")
}
