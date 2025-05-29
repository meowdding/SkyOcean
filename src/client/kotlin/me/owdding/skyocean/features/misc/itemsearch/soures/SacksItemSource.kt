package me.owdding.skyocean.features.misc.itemsearch.soures

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.misc.itemsearch.ItemContext
import me.owdding.skyocean.features.misc.itemsearch.TrackedItem
import me.owdding.skyocean.utils.Utils.mapNotNull
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.profile.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient

object SacksItemSource : ItemSource {
    override fun getAll() = SacksAPI.sackItems.mapNotNull(
        { (id) ->
            SkyOcean.warn("Couldn't find item for {}", id)
        },
        { (id, amount) ->
            RepoItemsAPI.getItemOrNull(id)?.copyWithCount(amount)
        },
    ).map { TrackedItem(it, SackItemContext) }

    override fun remove(item: TrackedItem) {
        TODO("Not yet implemented")
    }

    override val type = ItemSources.SACKS
}

object SackItemContext : ItemContext {
    override fun collectLines(): List<Component> = emptyList()

    override fun open() = McClient.sendCommand("sacks")
}
